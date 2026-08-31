package resv.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import member.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resv.dto.*;
import resv.entity.Resv;
import resv.enums.ResvStatus;
import resv.exception.ResvErrorCode;
import resv.exception.ResvException;
import resv.exception.ResvValidationException;
import resv.repository.ResvRepository;
import resv.repository.ResvRoomUnitRepository;
import room.entity.Room;
import room.entity.RoomUnit;
import room.entity.enums.RoomUnitStatus;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResvServiceImpl implements ResvService {

    private static final int RESV_NUMBER_RETRY_LIMIT = 5;
    private static final DateTimeFormatter RESV_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final EntityManager entityManager;
    private final ResvRepository resvRepository;
    private final ResvRoomUnitRepository resvRoomUnitRepository;
    private final Clock resvClock;

    @Override
    @Transactional
    public CreateResvResponseDTO create(String memberEmail, CreateResvRequestDTO request) {
        User member = findMember(memberEmail);

        Room room = entityManager.find(Room.class, request.getRoomId());
        if (room == null) {
            throw new ResvException(ResvErrorCode.ROOM_NOT_FOUND);
        }
        if (request.getGuestCount() > room.getMaxGuests()) {
            throw new ResvValidationException("guest_count", "CapacityExceeded", "객실의 최대 허용 인원을 초과했습니다.");
        }

        RoomUnit roomUnit = resvRoomUnitRepository.findAvailableForUpdate(
                        room.getId(), request.getCheckInDate(), request.getCheckOutDate(),
                        // 판매 가능 상태가 확정될 때까지 ACTIVE만 예약 가능으로 가정한다.
                        RoomUnitStatus.ACTIVE, ResvStatus.RESERVED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResvException(ResvErrorCode.ROOM_NOT_AVAILABLE));

        int nights = Math.toIntExact(ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate()));
        int pricePerNight = room.getBasePrice();
        int totalPrice = Math.multiplyExact(pricePerNight, nights);

        Resv saved = resvRepository.save(Resv.builder()
                .roomUnitsId(roomUnit.getId())
                .memberId(member.getId())
                .resvNumber(nextResvNumber(request))
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .pricePerNight(pricePerNight)
                .totalPrice(totalPrice)
                .resvStatus(ResvStatus.RESERVED)
                .build());

        return CreateResvResponseDTO.from(saved, room, roomUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public ResvAvailabilityResponseDTO getAvailability(ResvAvailabilityRequestDTO request) {
        return ResvAvailabilityResponseDTO.of(request, resvRoomUnitRepository.findAvailability(
                request.getCheckInDate(), request.getCheckOutDate(), request.getGuestCount(),
                RoomUnitStatus.ACTIVE, ResvStatus.RESERVED));
    }

    @Override
    @Transactional(readOnly = true)
    public MyResvListResponseDTO getMyReservations(String memberEmail, MyResvSearchRequestDTO request) {
        User member = findMember(memberEmail);
        PageRequest pageable = pageRequest(request.getPageNum(), request.getPageSize());
        Page<Resv> page = request.getResvStatus() == null
                ? resvRepository.findByMemberId(member.getId(), pageable)
                : resvRepository.findByMemberIdAndResvStatus(member.getId(), request.getResvStatus(), pageable);
        return MyResvListResponseDTO.of(page.map(this::toMyListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public MyResvDetailResponseDTO getMyReservation(String memberEmail, long resvId) {
        User member = findMember(memberEmail);
        Resv resv = findReservation(resvId);
        verifyOwner(resv, member);
        return toMyDetail(resv);
    }

    @Override
    @Transactional
    public ResvCancelResponseDTO cancelMyReservation(String memberEmail, long resvId) {
        User member = findMember(memberEmail);
        Resv resv = findReservation(resvId);
        verifyOwner(resv, member);
        return cancel(resv);
    }

    @Override
    @Transactional(readOnly = true)
    public ResvListResponseDTO getAdminReservations(AdminResvSearchRequestDTO request) {
        String keyword = request.getKeyword() == null || request.getKeyword().isBlank() ? null : request.getKeyword().trim();
        Page<Resv> page = resvRepository.searchForAdmin(request.getResvStatus(), request.getSearchFromDate(),
                request.getSearchToDate(), keyword, pageRequest(request.getPageNum(), request.getPageSize()));
        return ResvListResponseDTO.of(page.map(this::toAdminListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public ResvDetailResponseDTO getAdminReservation(long resvId) {
        return toAdminDetail(findReservation(resvId));
    }

    @Override
    @Transactional
    public ResvCancelResponseDTO cancelAdminReservation(long resvId) {
        return cancel(findReservation(resvId));
    }

    private ResvCancelResponseDTO cancel(Resv resv) {
        LocalDateTime now = LocalDateTime.now(resvClock);
        if (resv.getResvStatus() != ResvStatus.RESERVED || !LocalDate.now(resvClock).isBefore(resv.getCheckInDate())) {
            throw new ResvException(ResvErrorCode.RESV_CANNOT_CANCEL);
        }
        resv.cancel(now);
        return new ResvCancelResponseDTO(resv.getResvId(), resv.getResvNumber(), resv.getResvStatus().name(), resv.getCancelledAt());
    }

    private User findMember(String memberEmail) {
        return entityManager.createQuery("select user from User user where user.email = :email", User.class)
                .setParameter("email", memberEmail.trim().toLowerCase(Locale.ROOT))
                .getResultStream().findFirst()
                .orElseThrow(() -> new ResvException(ResvErrorCode.AUTH_UNAUTHORIZED));
    }

    private Resv findReservation(long resvId) {
        return resvRepository.findById(resvId).orElseThrow(() -> new ResvException(ResvErrorCode.RESV_NOT_FOUND));
    }

    private void verifyOwner(Resv resv, User member) {
        if (!resv.getMemberId().equals(member.getId())) {
            throw new ResvException(ResvErrorCode.RESV_ACCESS_DENIED);
        }
    }

    private PageRequest pageRequest(Integer pageNum, Integer pageSize) {
        return PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("resvId")));
    }

    private RoomUnit roomUnit(Resv resv) {
        return resvRoomUnitRepository.findById(resv.getRoomUnitsId())
                .orElseThrow(() -> new ResvException(ResvErrorCode.RESV_NOT_FOUND));
    }

    private User member(Resv resv) {
        return entityManager.find(User.class, resv.getMemberId());
    }

    private MyResvListResponseDTO.MyResvListItemDTO toMyListItem(Resv resv) {
        RoomUnit roomUnit = roomUnit(resv);
        Room room = roomUnit.getRoom();
        return new MyResvListResponseDTO.MyResvListItemDTO(resv.getResvId(), resv.getResvNumber(), resv.getRoomUnitsId(),
                room.getId(), roomUnit.getRoomNumber(), room.getName(), resv.getCheckInDate(), resv.getCheckOutDate(),
                resv.getGuestCount(), resv.getTotalPrice(), resv.getResvStatus().name(), resv.getCreatedAt());
    }

    private ResvListResponseDTO.ResvListItemDTO toAdminListItem(Resv resv) {
        RoomUnit roomUnit = roomUnit(resv);
        Room room = roomUnit.getRoom();
        User member = member(resv);
        return new ResvListResponseDTO.ResvListItemDTO(resv.getResvId(), resv.getResvNumber(), resv.getMemberId(), member.getName(),
                resv.getRoomUnitsId(), room.getId(), roomUnit.getRoomNumber(), room.getName(), resv.getCheckInDate(),
                resv.getCheckOutDate(), resv.getGuestCount(), resv.getTotalPrice(), resv.getResvStatus().name(), resv.getCreatedAt());
    }

    private MyResvDetailResponseDTO toMyDetail(Resv resv) {
        RoomUnit roomUnit = roomUnit(resv);
        Room room = roomUnit.getRoom();
        return new MyResvDetailResponseDTO(resv.getResvId(), resv.getResvNumber(), resv.getMemberId(), resv.getRoomUnitsId(),
                room.getId(), roomUnit.getRoomNumber(), room.getName(), resv.getCheckInDate(), resv.getCheckOutDate(),
                resv.getGuestCount(), nights(resv), resv.getPricePerNight(), resv.getTotalPrice(), resv.getResvStatus().name(),
                resv.getCreatedAt(), resv.getCancelledAt());
    }

    private ResvDetailResponseDTO toAdminDetail(Resv resv) {
        RoomUnit roomUnit = roomUnit(resv);
        Room room = roomUnit.getRoom();
        User member = member(resv);
        return new ResvDetailResponseDTO(resv.getResvId(), resv.getResvNumber(), resv.getMemberId(), member.getName(), member.getPhoneNumber(),
                resv.getRoomUnitsId(), room.getId(), roomUnit.getRoomNumber(), room.getName(), resv.getCheckInDate(), resv.getCheckOutDate(),
                resv.getGuestCount(), nights(resv), resv.getPricePerNight(), resv.getTotalPrice(), resv.getResvStatus().name(),
                resv.getCreatedAt(), resv.getCancelledAt());
    }

    private int nights(Resv resv) {
        return Math.toIntExact(ChronoUnit.DAYS.between(resv.getCheckInDate(), resv.getCheckOutDate()));
    }

    private String nextResvNumber(CreateResvRequestDTO request) {
        for (int attempt = 0; attempt < RESV_NUMBER_RETRY_LIMIT; attempt++) {
            String number = "RSV-" + RESV_DATE_FORMAT.format(request.getCheckInDate()) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
            if (!resvRepository.existsByResvNumber(number)) {
                return number;
            }
        }
        throw new IllegalStateException("예약 번호 생성에 실패했습니다.");
    }
}

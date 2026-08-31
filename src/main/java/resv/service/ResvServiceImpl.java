package resv.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import member.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resv.dto.CreateResvRequestDTO;
import resv.dto.CreateResvResponseDTO;
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

    @Override
    @Transactional
    public CreateResvResponseDTO create(String memberEmail, CreateResvRequestDTO request) {
        User member = entityManager.createQuery("select user from User user where user.email = :email", User.class)
                .setParameter("email", memberEmail.trim().toLowerCase(Locale.ROOT))
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new ResvException(ResvErrorCode.AUTH_UNAUTHORIZED));

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

package member.user.service;

import lombok.RequiredArgsConstructor;
import member.common.exception.MemberErrorCode;
import member.common.exception.MemberException;
import member.inquiry.domain.Inquiry;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.dto.AdminUserActivityResponse;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserRoleUpdateRequest;
import member.user.dto.AdminUserStatsResponse;
import member.user.dto.UserUpdateRequest;
import member.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resv.repository.ResvRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AdminUserService 구현체.
 * 클래스 기본 트랜잭션은 readOnly=true로 두고, 변경이 일어나는 메서드에만 @Transactional을 덮어쓴다.
 *
 * 권한 변경에는 운영 중 락아웃을 막기 위한 안전장치를 둔다.
 * - 본인 계정의 권한은 바꿀 수 없다 (스스로 관리자에서 내려오는 실수 방지)
 * - 마지막 남은 관리자는 일반 회원으로 내릴 수 없다 (관리자가 0명이 되는 상황 방지)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final ResvRepository resvRepository;

    @Override
    public AdminUserListResponse getUsers(String adminEmail, String role, String keyword, int pageNum, int pageSize) {
        validateAdmin(adminEmail);

        // 값이 없는 필터는 null로 넘겨 쿼리에서 조건을 건너뛰게 한다.
        String normalizedRole = normalizeRoleFilter(role);
        String normalizedKeyword = blankToNull(keyword);
        Pageable pageable = toPageable(pageNum, pageSize);

        Page<User> users = userRepository.searchForAdmin(normalizedRole, normalizedKeyword, pageable);

        // 목록 행마다 건수를 세면 조회가 회원 수만큼 늘어나므로, 현재 페이지의 ID만 모아 한 번에 집계한다.
        List<Long> userIds = users.getContent().stream().map(User::getId).toList();
        Map<Long, Long> reservationCounts = countsByOwnerId(userIds, resvRepository::countByMemberIds);
        Map<Long, Long> inquiryCounts = countsByOwnerId(userIds, inquiryRepository::countByUserIds);

        return AdminUserListResponse.of(users.map(user -> new AdminUserListResponse.AdminUserListItem(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                reservationCounts.getOrDefault(user.getId(), 0L),
                inquiryCounts.getOrDefault(user.getId(), 0L),
                user.getCreatedAt())));
    }

    @Override
    public AdminUserDetailResponse getUser(String adminEmail, Long userId) {
        validateAdmin(adminEmail);
        return toDetail(findUserById(userId));
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUser(String adminEmail, Long userId, UserUpdateRequest request) {
        validateAdmin(adminEmail);

        User user = findUserById(userId);
        // 회원 본인 수정과 같은 도메인 로직을 사용한다(null/공백 필드는 무시하고 부분 반영).
        user.updateProfile(request.getName(), request.getPhoneNumber());
        return toDetail(user);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse changeRole(String adminEmail, Long userId, AdminUserRoleUpdateRequest request) {
        User admin = validateAdmin(adminEmail);

        String newRole = normalizeRole(request.getRole());
        if (!ADMIN_ROLE.equals(newRole) && !USER_ROLE.equals(newRole)) {
            throw new MemberException(MemberErrorCode.INVALID_ROLE);
        }

        User target = findUserById(userId);
        if (target.getId().equals(admin.getId())) {
            throw new MemberException(MemberErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }
        // 관리자를 일반 회원으로 내리는 경우, 내리고 나서도 관리자가 최소 1명은 남아야 한다.
        if (ADMIN_ROLE.equals(target.getRole()) && USER_ROLE.equals(newRole)
                && userRepository.countByRole(ADMIN_ROLE) <= 1) {
            throw new MemberException(MemberErrorCode.LAST_ADMIN_CANNOT_BE_DEMOTED);
        }

        target.changeRole(newRole);
        return toDetail(target);
    }

    @Override
    public AdminUserStatsResponse getStats(String adminEmail) {
        validateAdmin(adminEmail);

        long total = userRepository.count();
        long admins = userRepository.countByRole(ADMIN_ROLE);
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        return new AdminUserStatsResponse(
                total,
                admins,
                total - admins,
                userRepository.countByCreatedAtGreaterThanEqual(startOfToday),
                userRepository.countByCreatedAtGreaterThanEqual(startOfToday.minusDays(6)));
    }

    @Override
    public AdminUserActivityResponse getActivity(String adminEmail, Long userId) {
        validateAdmin(adminEmail);

        User user = findUserById(userId);
        List<AdminUserActivityResponse.ReservationItem> reservations =
                resvRepository.findSummariesByMemberId(user.getId()).stream()
                        .map(summary -> new AdminUserActivityResponse.ReservationItem(
                                summary.getResvId(),
                                summary.getResvNumber(),
                                summary.getRoomName(),
                                summary.getRoomNumber(),
                                summary.getCheckInDate(),
                                summary.getCheckOutDate(),
                                summary.getGuestCount(),
                                summary.getTotalPrice(),
                                summary.getResvStatus().name(),
                                summary.getCreatedAt()))
                        .toList();

        List<AdminUserActivityResponse.InquiryItem> inquiries =
                inquiryRepository.findAllByUserIdWithUser(user.getId()).stream()
                        .map(inquiry -> new AdminUserActivityResponse.InquiryItem(
                                inquiry.getId(),
                                inquiry.getTitle(),
                                inquiry.getStatus().name(),
                                inquiry.getAnsweredAt(),
                                inquiry.getCreatedAt()))
                        .toList();

        return new AdminUserActivityResponse(user.getId(), reservations, inquiries);
    }

    /**
     * [소유자 ID, 건수] 형태의 집계 결과를 조회에 바로 쓸 수 있는 Map으로 바꾼다.
     * 대상이 없으면 쿼리 자체를 보내지 않는다(빈 in 절 방지).
     */
    private Map<Long, Long> countsByOwnerId(List<Long> ownerIds, Function<List<Long>, List<Object[]>> query) {
        if (ownerIds.isEmpty()) return Map.of();
        return query.apply(ownerIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private AdminUserDetailResponse toDetail(User user) {
        return AdminUserDetailResponse.of(
                user,
                resvRepository.countByMemberId(user.getId()),
                inquiryRepository.countByUserId(user.getId()));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }

    // 관리자 권한 확인. 통과하면 조회한 관리자 엔티티를 그대로 돌려줘 본인 여부 판단에 재사용한다.
    private User validateAdmin(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new MemberException(MemberErrorCode.ADMIN_ONLY);
        }
        return user;
    }

    // 목록 필터의 role은 잘못된 값이 와도 오류 대신 "필터 없음"으로 처리해 목록이 비어 보이지 않게 한다.
    private String normalizeRoleFilter(String role) {
        String normalized = normalizeRole(role);
        return (ADMIN_ROLE.equals(normalized) || USER_ROLE.equals(normalized)) ? normalized : null;
    }

    private String normalizeRole(String role) {
        return (role == null) ? null : role.trim().toUpperCase();
    }

    private Pageable toPageable(int pageNum, int pageSize) {
        int page = Math.max(pageNum, 0);
        int size = Math.clamp(pageSize, 1, MAX_PAGE_SIZE);
        // 최근 가입한 회원이 위로 오도록 고정 정렬한다.
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}

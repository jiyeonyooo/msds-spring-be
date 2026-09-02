package member.user.service;

import member.user.dto.AdminUserActivityResponse;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserStatsResponse;
import member.user.dto.AdminUserRoleUpdateRequest;
import member.user.dto.UserUpdateRequest;

/**
 * 관리자 전용 회원 관리 서비스.
 * 모든 메서드의 adminEmail은 JWT에서 추출한 로그인 관리자의 식별자이며,
 * 서비스 진입 시 role이 실제 ADMIN인지 한 번 더 확인한다(SecurityConfig와 이중 방어).
 */
public interface AdminUserService {

    // 회원 목록 조회 (권한 필터 + 이메일/이름/전화번호 키워드 검색 + 페이징)
    AdminUserListResponse getUsers(String adminEmail, String role, String keyword, int pageNum, int pageSize);

    // 회원 상세 조회 (예약/문의 건수 포함)
    AdminUserDetailResponse getUser(String adminEmail, Long userId);

    // 관리자에 의한 회원 정보 정정 (이름, 전화번호)
    AdminUserDetailResponse updateUser(String adminEmail, Long userId, UserUpdateRequest request);

    // 회원 권한 변경 (USER <-> ADMIN)
    AdminUserDetailResponse changeRole(String adminEmail, Long userId, AdminUserRoleUpdateRequest request);

    // 대시보드용 회원 집계
    AdminUserStatsResponse getStats(String adminEmail);

    // 회원의 예약/문의 이력
    AdminUserActivityResponse getActivity(String adminEmail, Long userId);
}

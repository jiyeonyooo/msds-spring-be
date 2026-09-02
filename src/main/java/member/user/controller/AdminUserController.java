package member.user.controller;

import global.dto.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import member.user.dto.AdminUserActivityResponse;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserRoleUpdateRequest;
import member.user.dto.AdminUserStatsResponse;
import member.user.dto.UserUpdateRequest;
import member.user.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 회원 관리 API.
 * 접근 제어는 SecurityConfig의 /api/admin/** hasRole("ADMIN")에서 1차로 처리되고,
 * 서비스 계층에서 User.role을 한 번 더 확인한다(이중 방어).
 *
 * 목록 조회의 쿼리 파라미터와 응답의 페이징 봉투는 관리자 예약 API(/api/admin/resv)와
 * 같은 snake_case 규격을 사용한다.
 */
@RestController
@RequestMapping("/api/admin/users")
@Validated
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 1. 회원 목록 조회 - GET /api/admin/users
     * role(USER/ADMIN)로 권한을 거르고, keyword로 이메일·이름·전화번호를 검색한다.
     * 예) GET /api/admin/users?role=ADMIN&keyword=hong&page_num=0&page_size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResponse>> getUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "page_num", defaultValue = "0") int pageNum,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize) {

        AdminUserListResponse response =
                adminUserService.getUsers(userDetails.getUsername(), role, keyword, pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.success("회원 목록 조회에 성공했습니다.", response));
    }

    // 2. 회원 집계 조회 - GET /api/admin/users/stats
    // 경로 충돌을 피하기 위해 /{userId}보다 먼저 선언한다.
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminUserStatsResponse>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        AdminUserStatsResponse response = adminUserService.getStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("회원 통계 조회에 성공했습니다.", response));
    }

    // 3. 회원 상세 조회 - GET /api/admin/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "회원 ID는 1 이상의 값이어야 합니다.") Long userId) {

        AdminUserDetailResponse response = adminUserService.getUser(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.success("회원 상세 조회에 성공했습니다.", response));
    }

    // 4. 회원 활동 내역 조회 - GET /api/admin/users/{userId}/activity
    @GetMapping("/{userId}/activity")
    public ResponseEntity<ApiResponse<AdminUserActivityResponse>> getActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "회원 ID는 1 이상의 값이어야 합니다.") Long userId) {

        AdminUserActivityResponse response = adminUserService.getActivity(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.success("회원 활동 내역 조회에 성공했습니다.", response));
    }

    // 5. 회원 정보 정정 - PATCH /api/admin/users/{userId}
    // 회원 본인 수정과 동일하게 이름/전화번호만 부분 수정한다.
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "회원 ID는 1 이상의 값이어야 합니다.") Long userId,
            @Valid @RequestBody UserUpdateRequest request) {

        AdminUserDetailResponse response = adminUserService.updateUser(userDetails.getUsername(), userId, request);
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다.", response));
    }

    // 6. 회원 권한 변경 - PATCH /api/admin/users/{userId}/role
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> changeRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "회원 ID는 1 이상의 값이어야 합니다.") Long userId,
            @Valid @RequestBody AdminUserRoleUpdateRequest request) {

        AdminUserDetailResponse response = adminUserService.changeRole(userDetails.getUsername(), userId, request);
        return ResponseEntity.ok(ApiResponse.success("회원 권한이 변경되었습니다.", response));
    }
}

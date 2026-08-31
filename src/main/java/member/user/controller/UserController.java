package member.user.controller;

import global.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;
import member.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 회원(User) 관련 API 컨트롤러.
 * 인증된 사용자 본인의 정보 조회/수정/탈퇴를 담당한다.
 * 모든 엔드포인트는 인증이 필요하며, @AuthenticationPrincipal을 통해
 * Spring Security가 인증 필터에서 채워준 로그인 사용자 정보(UserDetails)를 주입받는다.
 * 응답은 공통 규격인 ApiResponse(code, message, data)로 감싸서 내려준다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 1. 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        UserResponse response = userService.getMyProfile(email);
        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회에 성공했습니다.", response));
    }

    /**
     * 2. 내 정보 수정
     * PATCH /api/users/me
     *
     * 이름/전화번호 등 일부 필드만 부분 수정한다(Dirty Checking 기반).
     * @Valid를 통해 요청 필드(UserUpdateRequest)의 형식(전화번호 패턴 등)을 검증한다.
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {

        String email = userDetails.getUsername();
        UserUpdateResponse response = userService.updateMyProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 성공적으로 수정되었습니다.", response));
    }

    /**
     * 3. 회원 탈퇴
     * DELETE /api/users/me
     *
     * 본인 확인을 위해 비밀번호를 재입력받아 검증한 후 계정을 삭제한다.
     * 삭제 성공 시 별도 응답 데이터 없이 안내 메시지만 반환한다.
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDeleteRequest request) {

        String email = userDetails.getUsername();
        userService.deleteUser(email, request);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
    }
}

package member.user.controller;

import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;
import member.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 회원(User) 관련 API 컨트롤러.
 * 인증된 사용자 본인의 정보 조회/수정/탈퇴를 담당한다.
 * 모든 엔드포인트는 인증이 필요하며, @AuthenticationPrincipal을 통해
 * Spring Security가 인증 필터에서 채워준 로그인 사용자 정보(UserDetails)를 주입받는다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 1. 내 정보 조회
     * GET /users/me
     *
     * 인증된 사용자의 이메일(=로그인 ID)을 기준으로 프로필 정보를 조회한다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserResponse response = userService.getMyProfile(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 내 정보 수정
     * PATCH /users/me
     *
     * 이름/전화번호 등 일부 필드만 부분 수정한다(Dirty Checking 기반).
     * @Valid를 통해 요청 필드(UserUpdateRequest)의 형식(전화번호 패턴 등)을 검증한다.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserUpdateResponse> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {

        String email = userDetails.getUsername();
        UserUpdateResponse response = userService.updateMyProfile(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 회원 탈퇴
     * DELETE /users/me
     *
     * 본인 확인을 위해 비밀번호를 재입력받아 검증한 후 계정을 삭제한다.
     * 삭제 성공 시 별도 응답 바디 없이 안내 메시지만 반환한다.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDeleteRequest request) {

        String email = userDetails.getUsername();
        userService.deleteUser(email, request);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }
}
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 1. 내 정보 조회
     * GET /users/me
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
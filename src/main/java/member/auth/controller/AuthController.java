package member.auth.controller;

import global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import member.auth.dto.*;
import member.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증(회원가입/로그인/로그아웃) API.
 * 모든 응답은 프로젝트 공통 응답 규격인 ApiResponse(code, message, data)로 감싸서 내려준다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증")
public class AuthController {

    private final AuthService authService;

    /**
     * 1. 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일과 회원 정보를 받아 일반 회원 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 2. 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 JWT access token을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    /**
     * 3. 로그아웃
     * POST /api/auth/logout
     *
     * 서버에 저장하는 상태가 없으므로 별도 응답 데이터 없이 안내 메시지만 반환한다.
     */
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "서버 상태를 변경하지 않습니다. 클라이언트가 보관 중인 access token을 삭제하면 로그아웃이 완료됩니다."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "선택 사항인 기존 Authorization 헤더", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다.", null));
    }
}

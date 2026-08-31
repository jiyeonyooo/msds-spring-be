package member.common.exception;

import global.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * member 패키지 컨트롤러 전용 예외 처리기.
 * basePackages를 "member"로 한정했기 때문에 다른 팀 패키지(room, resv 등)의 응답에는 영향을 주지 않는다.
 *
 * 모든 실패 응답도 성공 응답과 동일하게 ApiResponse(code, message, data) 형태로 통일한다.
 * 실패 시 data는 항상 null이며, code에는 MemberErrorCode의 이름이 들어간다.
 */
@Slf4j
@RestControllerAdvice(basePackages = "member")
public class MemberExceptionHandler {

    // 서비스 계층에서 의도적으로 던진 비즈니스 예외
    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(MemberException e) {
        MemberErrorCode errorCode = e.getErrorCode();
        return toResponse(errorCode, e.getMessage());
    }

    // @Valid 검증 실패 (요청 DTO의 @NotBlank, @Email, @Pattern 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = MemberErrorCode.INVALID_INPUT.getMessage();
        }
        return toResponse(MemberErrorCode.INVALID_INPUT, message);
    }

    // 컨트롤러 호출 단계에서 권한이 부족한 경우(@PreAuthorize 등)는 500이 아닌 403으로 내려준다.
    // (시큐리티 필터 단계의 인가 실패는 JwtAccessDeniedHandler가 처리)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return toResponse(MemberErrorCode.FORBIDDEN, MemberErrorCode.FORBIDDEN.getMessage());
    }

    // 쿼리 파라미터/경로 변수의 타입이 맞지 않는 경우 (예: ?status=UNKNOWN, /api/inquiries/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = "'" + e.getName() + "' 파라미터 값이 올바르지 않습니다.";
        return toResponse(MemberErrorCode.INVALID_INPUT, message);
    }

    // 그 외 예상하지 못한 예외는 내부 메시지를 노출하지 않고 500으로 통일
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("[member] 처리되지 않은 예외 발생", e);
        return toResponse(MemberErrorCode.INTERNAL_ERROR, MemberErrorCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(MemberErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getCode(), message, null));
    }
}

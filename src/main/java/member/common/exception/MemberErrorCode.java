package member.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * member 패키지에서 사용하는 에러 코드 정의.
 * ApiResponse(code, message, data)의 code/message 값과 HTTP 상태 코드를 한 곳에서 관리한다.
 * (기존에는 모든 실패가 IllegalArgumentException -> 500 으로 내려가 클라이언트가 원인을 구분할 수 없었다)
 */
@Getter
public enum MemberErrorCode {

    // 회원
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 인증
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자 권한이 없습니다."),

    // 문의
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 문의입니다."),
    INQUIRY_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 작성한 문의만 조회할 수 있습니다."),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    MemberErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    // ApiResponse의 code 필드에 담기는 값 (예: "USER_NOT_FOUND")
    public String getCode() {
        return name();
    }
}

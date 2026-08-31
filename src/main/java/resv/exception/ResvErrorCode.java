package resv.exception;

import org.springframework.http.HttpStatus;

public enum ResvErrorCode {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실 정보를 찾을 수 없습니다."),
    ROOM_NOT_AVAILABLE(HttpStatus.CONFLICT, "선택한 기간에 예약 가능한 객실이 없습니다."),
    RESV_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
    RESV_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 예약에 접근할 권한이 없습니다."),
    RESV_CANNOT_CANCEL(HttpStatus.CONFLICT, "취소할 수 없는 예약입니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");

    private final HttpStatus status;
    private final String message;

    ResvErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

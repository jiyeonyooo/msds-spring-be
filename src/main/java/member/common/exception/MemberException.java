package member.common.exception;

import lombok.Getter;

/**
 * member 패키지 공통 예외.
 * 서비스 계층은 이 예외만 던지고, HTTP 상태 코드/응답 변환은 MemberExceptionHandler가 담당한다.
 */
@Getter
public class MemberException extends RuntimeException {

    private final MemberErrorCode errorCode;

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 기본 메시지 대신 상황에 맞는 메시지를 내려주고 싶을 때 사용
    public MemberException(MemberErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

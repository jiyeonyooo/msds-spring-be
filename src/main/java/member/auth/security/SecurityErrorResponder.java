package member.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import member.common.exception.MemberErrorCode;

import java.io.IOException;

/**
 * 시큐리티 필터 단계(인증/인가 실패)에서 발생한 오류를
 * 컨트롤러 응답과 동일한 규격(ApiResponse: code, message, data)의 JSON으로 내려주기 위한 헬퍼.
 *
 * 필터 예외는 @RestControllerAdvice까지 도달하지 않아 직접 응답을 써야 하고,
 * 내려보내는 값이 MemberErrorCode에 정의된 고정 문자열뿐이라 직렬화 라이브러리 없이 문자열로 작성한다.
 */
final class SecurityErrorResponder {

    private SecurityErrorResponder() {
    }

    static void write(HttpServletResponse response, MemberErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());
        response.getWriter().write(
                "{\"code\":\"" + escape(errorCode.getCode()) + "\","
                        + "\"message\":\"" + escape(errorCode.getMessage()) + "\","
                        + "\"data\":null}"
        );
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

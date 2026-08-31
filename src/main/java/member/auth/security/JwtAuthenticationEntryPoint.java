package member.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import member.common.exception.MemberErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 요청이 보호된 API에 접근했을 때 호출된다.
 * 스프링 기본 동작(로그인 페이지로 리다이렉트) 대신,
 * 컨트롤러 응답과 동일한 규격(ApiResponse)의 401 JSON을 내려준다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        SecurityErrorResponder.write(response, MemberErrorCode.UNAUTHORIZED);
    }
}

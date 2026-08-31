package member.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import member.common.exception.MemberErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 됐지만 권한이 부족한 요청(예: 일반 회원이 /api/admin/** 접근)에 대해
 * 스프링 기본 403 응답 대신 공통 응답 규격(ApiResponse)의 JSON을 내려준다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        SecurityErrorResponder.write(response, MemberErrorCode.FORBIDDEN);
    }
}

package member.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 발급/검증 담당 컴포넌트.
 * refresh token 없이 access token 하나만 사용하는 정책에 맞춘 최소 구현.
 * jjwt 0.12.x API 기준.
 *
 * application.yml 예시:
 * jwt:
 *   secret: <최소 32바이트 이상의 랜덤 문자열>
 *   access-token-validity-ms: 7200000   # 2시간 (선택, 미설정 시 기본값 사용)
 *
 * build.gradle:
 * implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
 * runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
 * runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:7200000}") long validityInMillis // 기본 2시간
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMillis = validityInMillis;
    }

    // 로그인 성공 시 호출. subject에 이메일, claim에 role을 담아 발급.
    public String createToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // JWT 인증 필터에서 토큰으로부터 이메일(subject)을 꺼낼 때 사용.
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // JWT 인증 필터에서 role claim을 꺼낼 때 사용.
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // JWT 인증 필터에서 토큰 유효성(서명/만료) 검증 시 사용.
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
package member.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 * - 세션 미사용(STATELESS), 폼로그인/기본인증 비활성화
 * - /api/auth/** 는 인증 없이 접근 가능 (회원가입/로그인)
 * - /api/admin/** 는 ROLE_ADMIN만 접근 가능
 * - 인증/인가 실패 응답도 공통 규격(ApiResponse)의 JSON으로 통일
 * - 그 외 모든 요청은 인증 필요
 * - JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞단에 배치
 *
 * 주의: 프로젝트 다른 곳에 이미 PasswordEncoder Bean이 등록되어 있다면
 * (예: 별도 PasswordConfig 클래스) 중복 Bean 정의 에러가 나므로 하나만 남겨야 한다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 등을 쓸 경우를 대비해 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401: 인증 없음/토큰 무효
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 403: 권한 부족
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/wellness/questions", "/api/wellness/guest/**").permitAll()
                        .requestMatchers("/api/rooms", "/api/rooms/**", "/api/facilities", "/api/facilities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/meditation/program", "/meditation/program/detail/*", "/meditation/review").permitAll()
                        .requestMatchers("/meditation/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/quietness/guesthouses/*/summary",
                                "/api/quietness/guesthouses/*/spaces",
                                "/api/quietness/guesthouses/*/spaces/*",
                                "/api/quietness/guesthouses/*/spaces/*/hourly",
                                "/api/quietness/spaces/*/history"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

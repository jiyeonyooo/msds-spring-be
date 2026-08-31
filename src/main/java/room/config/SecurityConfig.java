package room.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain roomSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/rooms",
                                "/api/rooms/**",
                                "/api/facilities",
                                "/api/facilities/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}

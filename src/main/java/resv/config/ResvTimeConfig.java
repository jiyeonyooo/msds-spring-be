package resv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ResvTimeConfig {

    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    Clock resvClock() {
        return Clock.system(KOREA_ZONE);
    }
}

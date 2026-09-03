package global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI meditationOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 응답의 accessToken 값만 입력하세요. Bearer 접두사는 자동으로 붙습니다.");

        return new OpenAPI()
                .info(new Info()
                        .title("MSDS Meditation API")
                        .description("""
                                명상 숙소 서비스의 REST API 문서입니다.

                                인증이 필요한 API는 먼저 `POST /api/auth/login`을 호출한 뒤,
                                우측 상단 **Authorize** 버튼에 응답의 `accessToken` 값만 입력해 테스트할 수 있습니다.
                                관리자 API는 `ADMIN` 권한이 있는 계정의 토큰이 필요합니다.
                                """)
                        .version("v1"))
                .servers(List.of(new Server().url("/").description("현재 서버")))
                .tags(List.of(
                        tag("인증", "회원가입, 로그인, 로그아웃"),
                        tag("회원", "인증 회원의 프로필 관리"),
                        tag("문의", "인증 회원의 1:1 문의"),
                        tag("객실", "공개 객실 조회"),
                        tag("편의시설", "공개 편의시설 조회"),
                        tag("예약", "객실 예약 조회 및 관리"),
                        tag("명상 프로그램", "명상 프로그램, 신청, 후기"),
                        tag("마음 기록", "마음상태 검사와 변화 기록"),
                        tag("조용함", "공간별 조용함 지수와 통계"),
                        tag("관리자 - 회원", "관리자용 회원 관리"),
                        tag("관리자 - 문의", "관리자용 문의 및 답변 관리"),
                        tag("관리자 - 객실·시설", "관리자용 객실과 편의시설 관리"),
                        tag("관리자 - 이미지", "관리자용 객실과 편의시설 이미지 업로드"),
                        tag("관리자 - 예약", "관리자용 객실 예약 관리"),
                        tag("관리자 - 명상 프로그램", "관리자용 명상 프로그램 관리"),
                        tag("관리자 - 마음 기록", "관리자용 마음상태 통계"),
                        tag("관리자 - 조용함", "관리자용 조용함 공간, 기기, 기준값 관리")
                ))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerScheme));
    }

    private Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }
}

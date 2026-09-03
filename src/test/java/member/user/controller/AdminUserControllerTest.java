package member.user.controller;

import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserStatsResponse;
import member.user.service.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 관리자 회원 API의 요청 매핑과 응답 JSON 규격을 확인한다.
 * 화면이 그대로 쓰는 snake_case 파라미터/필드명은 오타가 나도 조용히 넘어가므로 여기서 고정한다.
 * 권한 검사는 서비스 계층 테스트가 담당하므로 여기서는 보안 필터를 끄고 컨트롤러만 본다.
 */
@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    // 진입점(MeditationApplication)이 member 패키지의 상위가 아니라 슬라이스 테스트가 설정을 찾지 못한다.
    // 다른 테스트에 영향이 없도록 이 테스트가 볼 컨트롤러만 스캔하는 최소 설정을 둔다.
    // TypeExcludeFilter를 함께 걸어야 @WebMvcTest(controllers = ...)의 대상 밖 컨트롤러가 제외된다.
    // (없으면 같은 패키지의 UserController까지 올라와 UserService 빈을 찾지 못한다)
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(
            basePackageClasses = AdminUserController.class,
            excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
    static class TestConfiguration {

        // 보안 필터를 끈 슬라이스라서 @AuthenticationPrincipal을 풀어줄 리졸버를 직접 등록한다.
        @Bean
        WebMvcConfigurer authenticationPrincipalResolver() {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(new AuthenticationPrincipalArgumentResolver());
                }
            };
        }
    }

    private static final String ADMIN_EMAIL = "admin@example.com";

    // 컨트롤러가 userDetails.getUsername()으로 꺼내 쓰는 로그인 관리자를 심어둔다.
    @BeforeEach
    void signInAsAdmin() {
        UserDetails admin = org.springframework.security.core.userdetails.User
                .withUsername(ADMIN_EMAIL).password("encoded").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    private AdminUserDetailResponse detail(String role) {
        return new AdminUserDetailResponse(
                7L, "member@example.com", "홍길동", "010-1111-2222", role, 3, 2,
                LocalDateTime.of(2026, 9, 1, 14, 22), LocalDateTime.of(2026, 9, 2, 9, 30));
    }

    @Test
    @DisplayName("회원 목록은 snake_case 페이징 파라미터를 받아 같은 규격으로 응답한다")
    void getUsers() throws Exception {
        AdminUserListResponse.AdminUserListItem item = new AdminUserListResponse.AdminUserListItem(
                7L, "member@example.com", "홍길동", "010-1111-2222", "USER", 3, 2,
                LocalDateTime.of(2026, 9, 1, 14, 22));
        given(adminUserService.getUsers(eq(ADMIN_EMAIL), eq("USER"), eq("hong"), eq(2), eq(50)))
                .willReturn(new AdminUserListResponse(List.of(item), 2, 50, 1, 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("role", "USER")
                        .param("keyword", "hong")
                        .param("page_num", "2")
                        .param("page_size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.page_num").value(2))
                .andExpect(jsonPath("$.data.page_size").value(50))
                .andExpect(jsonPath("$.data.total_elements").value(1))
                .andExpect(jsonPath("$.data.user_list[0].user_id").value(7))
                .andExpect(jsonPath("$.data.user_list[0].phone_number").value("010-1111-2222"))
                .andExpect(jsonPath("$.data.user_list[0].reservation_count").value(3))
                .andExpect(jsonPath("$.data.user_list[0].inquiry_count").value(2))
                .andExpect(jsonPath("$.data.user_list[0].created_at").value("2026-09-01 14:22:00"));
    }

    @Test
    @DisplayName("페이징 파라미터를 생략하면 0페이지 20건이 기본값이다")
    void getUsers_defaultPaging() throws Exception {
        given(adminUserService.getUsers(eq(ADMIN_EMAIL), isNull(), isNull(), eq(0), eq(20)))
                .willReturn(new AdminUserListResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user_list").isArray());
    }

    @Test
    @DisplayName("/stats 경로가 회원 ID 경로보다 먼저 매칭된다")
    void getStats() throws Exception {
        given(adminUserService.getStats(eq(ADMIN_EMAIL)))
                .willReturn(new AdminUserStatsResponse(10, 2, 8, 1, 4));

        mockMvc.perform(get("/api/admin/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total_users").value(10))
                .andExpect(jsonPath("$.data.admin_users").value(2))
                .andExpect(jsonPath("$.data.general_users").value(8))
                .andExpect(jsonPath("$.data.new_users_today").value(1))
                .andExpect(jsonPath("$.data.new_users_last_7_days").value(4));
    }

    @Test
    @DisplayName("회원 상세는 활동 건수를 함께 내려준다")
    void getUser() throws Exception {
        given(adminUserService.getUser(eq(ADMIN_EMAIL), eq(7L))).willReturn(detail("USER"));

        mockMvc.perform(get("/api/admin/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user_id").value(7))
                .andExpect(jsonPath("$.data.reservation_count").value(3))
                .andExpect(jsonPath("$.data.updated_at").value("2026-09-02 09:30:00"));
    }

    @Test
    @DisplayName("권한 변경 요청 본문의 role이 서비스로 그대로 전달된다")
    void changeRole() throws Exception {
        given(adminUserService.changeRole(eq(ADMIN_EMAIL), eq(7L), org.mockito.ArgumentMatchers.argThat(
                request -> "ADMIN".equals(request.getRole())))).willReturn(detail("ADMIN"));

        mockMvc.perform(patch("/api/admin/users/7/role")
                        .contentType("application/json")
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("권한 값이 비어 있으면 400으로 거른다")
    void changeRole_blank() throws Exception {
        mockMvc.perform(patch("/api/admin/users/7/role")
                        .contentType("application/json")
                        .content("{\"role\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}

package member.user.controller;

import com.example.meditation.MeditationApplication;
import member.auth.service.JwtTokenProvider;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MeditationApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-user-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=admin-user-api-test-secret-key-1234567890"
})
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User member;

    @BeforeEach
    void setUp() {
        member = userRepository.save(user("member@example.com", "박시우", "USER"));
        userRepository.save(user("admin@example.com", "테스트 관리자", "ADMIN"));
    }

    @Test
    void 관리자가_회원_목록을_검색하고_상세를_조회한다() throws Exception {
        String adminToken = token("admin@example.com", "ADMIN");

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "박시우")
                        .param("role", "USER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user_list.length()").value(1))
                .andExpect(jsonPath("$.data.user_list[0].email").value("member@example.com"))
                .andExpect(jsonPath("$.data.user_list[0].reservation_count").value(0))
                .andExpect(jsonPath("$.data.user_list[0].inquiry_count").value(0))
                .andExpect(jsonPath("$.data.total_elements").value(1));

        mockMvc.perform(get("/api/admin/users/{userId}", member.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("박시우"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void 일반_회원은_관리자_회원_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token("member@example.com", "USER")))
                .andExpect(status().isForbidden());
    }

    private String token(String email, String role) {
        return jwtTokenProvider.createToken(email, role);
    }

    private User user(String email, String name, String role) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .name(name)
                .phoneNumber("010-0000-0000")
                .role(role)
                .build();
    }
}

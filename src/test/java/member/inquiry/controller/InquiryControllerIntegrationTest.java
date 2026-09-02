package member.inquiry.controller;

import com.example.meditation.MeditationApplication;
import member.auth.service.JwtTokenProvider;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MeditationApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:inquiry-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=inquiry-api-test-secret-key-1234567890"
})
@AutoConfigureMockMvc
@Transactional
class InquiryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private InquiryRepository inquiryRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(user("member@example.com", "테스트 회원", "USER"));
        userRepository.save(user("admin@example.com", "테스트 관리자", "ADMIN"));
    }

    @Test
    void 회원_문의부터_관리자_답변까지_하나의_흐름으로_연결된다() throws Exception {
        String memberToken = token("member@example.com", "USER");
        String adminToken = token("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/inquiries")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"디지털 디톡스 문의\","
                                + "\"content\":\"휴대폰 보관 방식이 궁금합니다.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("WAITING"));

        long inquiryId = inquiryRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/admin/inquiries/{inquiryId}", inquiryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorEmail").value("member@example.com"))
                .andExpect(jsonPath("$.data.status").value("WAITING"));

        mockMvc.perform(patch("/api/admin/inquiries/{inquiryId}/answer", inquiryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerContent\":\"원하시는 분만 자율적으로 참여합니다.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ANSWERED"))
                .andExpect(jsonPath("$.data.answerContent")
                        .value("원하시는 분만 자율적으로 참여합니다."));

        mockMvc.perform(get("/api/inquiries/{inquiryId}", inquiryId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ANSWERED"))
                .andExpect(jsonPath("$.data.answerContent")
                        .value("원하시는 분만 자율적으로 참여합니다."));
    }

    @Test
    void 일반_회원은_관리자_문의_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/admin/inquiries")
                        .header("Authorization", "Bearer " + token("member@example.com", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 문의_본문은_이천자를_초과할_수_없다() throws Exception {
        mockMvc.perform(post("/api/inquiries")
                        .header("Authorization", "Bearer " + token("member@example.com", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"길이 제한 확인\",\"content\":\""
                                + "가".repeat(2001) + "\"}"))
                .andExpect(status().isBadRequest());
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

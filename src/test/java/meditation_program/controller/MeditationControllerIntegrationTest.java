package meditation_program.controller;

import com.example.meditation.MeditationApplication;
import meditation_program.entity.Program;
import meditation_program.repository.ProgramRepository;
import member.auth.service.JwtTokenProvider;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MeditationApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:meditation-program-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=meditation-program-api-test-secret-key-1234567890"
})
@AutoConfigureMockMvc
@Transactional
class MeditationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Program program;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .email("member@example.com")
                .password("encoded-password")
                .name("테스트 회원")
                .phoneNumber("010-0000-0000")
                .role("USER")
                .build());
        program = programRepository.save(Program.builder()
                .name("Morning Silence Meditation")
                .capacity(10)
                .build());
    }

    @Test
    void 로그인하지_않아도_프로그램_목록을_조회한다() throws Exception {
        mockMvc.perform(get("/meditation/program"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(program.getId()))
                .andExpect(jsonPath("$[0].name").value("Morning Silence Meditation"))
                .andExpect(jsonPath("$[0].remain").value(10));
    }

    @Test
    void JWT의_로그인_이메일로_프로그램을_신청한다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(post("/meditation/program")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/meditation/program/reservation/")));

        assertThat(programRepository.findById(program.getId()).orElseThrow().getRemain()).isEqualTo(9);
    }

    @Test
    void 로그인하지_않으면_프로그램을_신청할_수_없다() throws Exception {
        mockMvc.perform(post("/meditation/program")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }
}

package meditation_program.controller;

import com.example.meditation.MeditationApplication;
import meditation_program.entity.Program;
import meditation_program.repository.ProgramRepository;
import meditation_program.repository.ProgramReservationRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
    private ProgramReservationRepository programReservationRepository;

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
        userRepository.save(User.builder()
                .email("admin@example.com")
                .password("encoded-password")
                .name("테스트 관리자")
                .phoneNumber("010-1111-1111")
                .role("ADMIN")
                .build());
        program = programRepository.save(Program.builder()
                .name("Morning Silence Meditation")
                .capacity(10)
                .build());
    }

    @Test
    void 로그인하지_않아도_프로그램_목록을_조회한다() throws Exception {
        mockMvc.perform(get("/api/meditation/program"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(program.getId()))
                .andExpect(jsonPath("$.data[0].name").value("Morning Silence Meditation"))
                .andExpect(jsonPath("$.data[0].remain").value(10));
    }

    @Test
    void 로컬_프론트의_관리자_DELETE_사전_요청을_허용한다() throws Exception {
        mockMvc.perform(options("/api/meditation/admin/program/{programId}", program.getId())
                        .header("Origin", "http://127.0.0.1:5173")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("DELETE")));
    }

    @Test
    void 로그인하지_않아도_프로그램_상세를_조회한다() throws Exception {
        mockMvc.perform(get("/api/meditation/program/detail/{programId}", program.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(program.getId()))
                .andExpect(jsonPath("$.data.name").value("Morning Silence Meditation"))
                .andExpect(jsonPath("$.data.capacity").value(10));
    }

    @Test
    void 존재하지_않는_프로그램_상세는_404를_응답한다() throws Exception {
        mockMvc.perform(get("/api/meditation/program/detail/{programId}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 프로그램입니다."));
    }

    @Test
    void JWT의_로그인_이메일로_프로그램을_신청한다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").isNumber());

        assertThat(programRepository.findById(program.getId()).orElseThrow().getRemain()).isEqualTo(9);
    }

    @Test
    void 로그인하지_않으면_프로그램을_신청할_수_없다() throws Exception {
        mockMvc.perform(post("/api/meditation/program")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인한_회원은_본인의_프로그램_신청_내역을_조회한다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/meditation/program/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].programId").value(program.getId()))
                .andExpect(jsonPath("$.data[0].programName").value("Morning Silence Meditation"))
                .andExpect(jsonPath("$.data[0].quantity").value(2))
                .andExpect(jsonPath("$.data[0].status").value("RESERVED"));
    }

    @Test
    void 일반_회원은_관리자_프로그램_API에_접근할_수_없다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(get("/api/meditation/admin/program/{programId}/applications", program.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_프로그램_신청자_목록을_조회한다() throws Exception {
        String memberToken = jwtTokenProvider.createToken("member@example.com", "USER");
        String adminToken = jwtTokenProvider.createToken("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/meditation/admin/program/{programId}/applications", program.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].programId").value(program.getId()))
                .andExpect(jsonPath("$.data[0].name").value("테스트 회원"))
                .andExpect(jsonPath("$.data[0].email").value("member@example.com"))
                .andExpect(jsonPath("$.data[0].status").value("RESERVED"));
    }

    @Test
    void 관리자는_기존_신청_인원을_유지하며_프로그램_정원을_수정한다() throws Exception {
        String memberToken = jwtTokenProvider.createToken("member@example.com", "USER");
        String adminToken = jwtTokenProvider.createToken("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/meditation/admin/program/{programId}", program.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Silence\",\"capacity\":12}"))
                .andExpect(status().isOk());

        Program updated = programRepository.findById(program.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Silence");
        assertThat(updated.getCapacity()).isEqualTo(12);
        assertThat(updated.getRemain()).isEqualTo(10);
    }

    @Test
    void 잔여_인원보다_많이_신청하면_409를_응답한다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":11}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("잔여 인원이 부족합니다."));
    }

    @Test
    void 신청_내역이_있는_프로그램은_삭제할_수_없다() throws Exception {
        String memberToken = jwtTokenProvider.createToken("member@example.com", "USER");
        String adminToken = jwtTokenProvider.createToken("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/meditation/admin/program/{programId}", program.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("신청 내역이 있는 프로그램은 삭제할 수 없습니다."));
    }

    @Test
    void 프로그램_후기_응답에_예약자와_예약_식별자를_포함한다() throws Exception {
        String token = jwtTokenProvider.createToken("member@example.com", "USER");

        mockMvc.perform(post("/api/meditation/program")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + program.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        Long reservationId = programReservationRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/meditation/review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programReservationId\":" + reservationId +
                                ",\"content\":\"고요하고 편안했습니다.\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/meditation/review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].programReservationId").value(reservationId))
                .andExpect(jsonPath("$.data[0].userId").isNumber())
                .andExpect(jsonPath("$.data[0].userName").value("테스트 회원"))
                .andExpect(jsonPath("$.data[0].content").value("고요하고 편안했습니다."));
    }
}

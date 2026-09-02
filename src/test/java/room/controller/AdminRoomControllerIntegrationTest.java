package room.controller;

import com.example.meditation.MeditationApplication;
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
import room.repository.RoomRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MeditationApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-room-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=admin-room-api-test-secret-key-1234567890"
})
@AutoConfigureMockMvc
@Transactional
class AdminRoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.save(user("member@example.com", "테스트 회원", "USER"));
        userRepository.save(user("admin@example.com", "테스트 관리자", "ADMIN"));
    }

    @Test
    void 관리자가_객실과_시설을_등록하면_관리자와_공개_API에_반영된다() throws Exception {
        String adminToken = token("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"마음쉼 스위트",
                                  "description":"바다를 바라보는 조용한 객실",
                                  "roomType":"RETREAT",
                                  "status":"AVAILABLE",
                                  "minGuest":1,
                                  "maxGuest":2,
                                  "area":32.5,
                                  "basePrice":280000,
                                  "mainImageUrl":"https://example.com/room.jpg",
                                  "bedType":"QUEEN",
                                  "bedCount":1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roomSpecs.bedType").value("QUEEN"))
                .andExpect(jsonPath("$.data.images[0].imageUrl")
                        .value("https://example.com/room.jpg"));

        long roomId = roomRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/admin/rooms")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roomId").value(roomId));

        mockMvc.perform(get("/api/rooms/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.images[0].imageType").value("MAIN"));

        mockMvc.perform(post("/api/admin/facilities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"차 명상 라운지",
                                  "category":"FOOD",
                                  "description":"차를 마시며 쉬는 공간",
                                  "active":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(get("/api/admin/facilities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("차 명상 라운지"));
    }

    @Test
    void 일반_회원은_관리자_객실_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/admin/rooms")
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

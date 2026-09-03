package com.example.meditation.quietness.controller;

import com.example.meditation.MeditationApplication;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.QuietSpaceRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MeditationApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:quietness-admin-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=quietness-admin-api-test-secret-key-1234567890"
})
@AutoConfigureMockMvc
@Transactional
class AdminQuietnessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuietSpaceRepository quietSpaceRepository;

    @Autowired
    private NoiseDeviceRepository noiseDeviceRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.save(user("member@example.com", "테스트 회원", "USER"));
        userRepository.save(user("admin@example.com", "테스트 관리자", "ADMIN"));
    }

    @Test
    void 관리자가_공간과_기기와_측정값을_등록하면_공개_조용함_API에_반영된다() throws Exception {
        String adminToken = token("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/admin/quietness/spaces")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guesthouseId\":1,\"name\":\"마음쉼 명상실\","
                                + "\"type\":\"MEDITATION_ROOM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("마음쉼 명상실"))
                .andExpect(jsonPath("$.data.active").value(true));

        long spaceId = quietSpaceRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/admin/quietness/devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guesthouseId\":1,\"spaceId\":" + spaceId + ","
                                + "\"deviceName\":\"명상실 소음계\","
                                + "\"serialNumber\":\"API-SERIAL-1\","
                                + "\"modelName\":\"MODEL-API\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        long deviceId = noiseDeviceRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/admin/quietness/measurements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":" + deviceId + ",\"decibel\":34.5,"
                                + "\"measuredAt\":\"2026-09-02T10:30:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decibel").value(34.5));

        mockMvc.perform(post("/api/admin/quietness/measurements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":" + deviceId + ",\"decibel\":34.5,"
                                + "\"measuredAt\":\"2100-01-01T00:00:00\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/quietness/guesthouses/1/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].spaceId").value(spaceId))
                .andExpect(jsonPath("$.data[0].decibel").value(34.5))
                .andExpect(jsonPath("$.data[0].level").value("QUIET"));

        mockMvc.perform(patch("/api/admin/quietness/devices/{deviceId}/status", deviceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void 일반_회원은_관리자_조용함_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/admin/quietness/guesthouses/1/spaces")
                        .header("Authorization", "Bearer " + token("member@example.com", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_조용함_기준값을_조회하고_빈틈없이_수정할_수_있다() throws Exception {
        String adminToken = token("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/admin/quietness/spaces")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guesthouseId\":1,\"name\":\"기준값 테스트 공간\",\"type\":\"OTHER\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/quietness/guesthouses/1/thresholds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].level").value("VERY_QUIET"))
                .andExpect(jsonPath("$.data[4].level").value("VERY_LOUD"));

        mockMvc.perform(patch("/api/admin/quietness/guesthouses/1/thresholds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"veryQuietMax\":25.00,\"quietMax\":35.00,"
                                + "\"normalMax\":50.00,\"loudMax\":65.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].maxDecibel").value(25.0))
                .andExpect(jsonPath("$.data[1].minDecibel").value(25.01))
                .andExpect(jsonPath("$.data[4].minDecibel").value(65.01));
    }

    @Test
    void 역전된_조용함_기준값은_거부한다() throws Exception {
        mockMvc.perform(patch("/api/admin/quietness/guesthouses/1/thresholds")
                        .header("Authorization", "Bearer " + token("admin@example.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"veryQuietMax\":40.00,\"quietMax\":35.00,"
                                + "\"normalMax\":50.00,\"loudMax\":65.00}"))
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

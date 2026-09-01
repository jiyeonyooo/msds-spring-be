package com.example.meditation.quietness.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:quietness-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=quietness-api-integration-test-secret-key-1234567890",
        "app.quietness.demo.enabled=true",
        "app.quietness.demo.guesthouse-id=1"
})
@AutoConfigureMockMvc
class QuietnessApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 데모_숙소의_공간별_현재_조용함을_HTTP로_조회한다() throws Exception {
        mockMvc.perform(get("/api/quietness/guesthouses/1/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].spaceName").value("마음쉼 명상실"))
                .andExpect(jsonPath("$.data[0].spaceType").value("MEDITATION_ROOM"))
                .andExpect(jsonPath("$.data[0].decibel").value(34.20))
                .andExpect(jsonPath("$.data[0].level").value("QUIET"));
    }

    @Test
    void 데모_숙소의_종합_조용함을_HTTP로_조회한다() throws Exception {
        mockMvc.perform(get("/api/quietness/guesthouses/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.guesthouseId").value(1))
                .andExpect(jsonPath("$.data.measuredSpaceCount").value(3))
                .andExpect(jsonPath("$.data.level").value("NORMAL"));
    }
}

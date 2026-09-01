package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.NoiseDeviceCreateRequest;
import com.example.meditation.quietness.dto.request.NoiseMeasurementCreateRequest;
import com.example.meditation.quietness.dto.request.QuietSpaceCreateRequest;
import com.example.meditation.quietness.dto.response.NoiseDeviceResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceResponse;
import com.example.meditation.quietness.dto.response.SpaceQuietnessResponse;
import com.example.meditation.quietness.entity.QuietSpaceType;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuietnessAdminService.class, QuietnessService.class, QuietnessThresholdService.class})
class QuietnessFlowIntegrationTest {

    @Autowired
    private QuietnessAdminService adminService;

    @Autowired
    private QuietnessService quietnessService;

    @Autowired
    private QuietnessThresholdRepository thresholdRepository;

    @Test
    void 공간과_기기를_등록하고_측정값을_조회하는_전체_흐름이_동작한다() {
        QuietSpaceResponse space = adminService.createSpace(new QuietSpaceCreateRequest(
                1L,
                "마음쉼 명상실",
                QuietSpaceType.MEDITATION_ROOM
        ));
        NoiseDeviceResponse device = adminService.createDevice(new NoiseDeviceCreateRequest(
                1L,
                space.spaceId(),
                "명상실 소음계",
                "DEMO-SERIAL-1",
                "MODEL-DEMO"
        ));
        LocalDateTime measuredAt = LocalDateTime.of(2026, 9, 1, 14, 30);

        adminService.createMeasurement(new NoiseMeasurementCreateRequest(
                device.deviceId(),
                new BigDecimal("34.50"),
                measuredAt
        ));

        List<SpaceQuietnessResponse> spaces = quietnessService.getSpaces(1L);
        assertThat(spaces).singleElement().satisfies(response -> {
            assertThat(response.spaceName()).isEqualTo("마음쉼 명상실");
            assertThat(response.spaceType()).isEqualTo(QuietSpaceType.MEDITATION_ROOM);
            assertThat(response.decibel()).isEqualByComparingTo("34.50");
            assertThat(response.level()).isEqualTo(QuietnessLevel.QUIET);
            assertThat(response.measuredAt()).isEqualTo(measuredAt);
        });
        assertThat(thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(1L))
                .hasSize(5);
    }
}

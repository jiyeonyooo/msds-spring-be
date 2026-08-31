package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.response.GuesthouseQuietnessSummaryResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceRecommendationResponse;
import com.example.meditation.quietness.dto.response.SpaceQuietnessResponse;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuietnessServiceTest {

    private final NoiseMeasurementRepository measurementRepository = mock(NoiseMeasurementRepository.class);
    private final QuietnessThresholdRepository thresholdRepository = mock(QuietnessThresholdRepository.class);
    private final QuietnessService service = new QuietnessService(measurementRepository, thresholdRepository);

    private final LocalDateTime measuredAt = LocalDateTime.of(2026, 8, 31, 15, 0);

    @BeforeEach
    void setUp() {
        when(thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(
                        threshold(QuietnessLevel.QUIET, "0.00", "39.99", 1),
                        threshold(QuietnessLevel.NORMAL, "40.00", "60.00", 2),
                        threshold(QuietnessLevel.LOUD, "60.01", null, 3)
                ));
    }

    @Test
    void 공간별_최신값의_평균으로_숙소_종합지수를_계산한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(1L))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        GuesthouseQuietnessSummaryResponse response = service.getGuesthouseSummary(1L);

        assertThat(response.averageDecibel()).isEqualByComparingTo("40.00");
        assertThat(response.level()).isEqualTo(QuietnessLevel.NORMAL);
        assertThat(response.measuredSpaceCount()).isEqualTo(2);
    }

    @Test
    void 공간별_현재_조용함_목록을_반환한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(1L))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        List<SpaceQuietnessResponse> responses = service.getSpaces(1L);

        assertThat(responses).extracting(SpaceQuietnessResponse::spaceId).containsExactly(10L, 20L);
        assertThat(responses).extracting(SpaceQuietnessResponse::level)
                .containsExactly(QuietnessLevel.QUIET, QuietnessLevel.NORMAL);
    }

    @Test
    void 데시벨이_가장_낮은_공간을_추천한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(1L))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        QuietSpaceRecommendationResponse response = service.recommendQuietSpace(1L);

        assertThat(response.spaceId()).isEqualTo(10L);
        assertThat(response.decibel()).isEqualByComparingTo("30.00");
        assertThat(response.level()).isEqualTo(QuietnessLevel.QUIET);
    }

    @Test
    void 측정값이_없는_숙소는_조회할_수_없다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getGuesthouseSummary(1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    private NoiseMeasurement measurement(Long spaceId, String decibel) {
        return new NoiseMeasurement(null, 1L, spaceId, new BigDecimal(decibel), measuredAt);
    }

    private QuietnessThreshold threshold(
            QuietnessLevel level,
            String minimum,
            String maximum,
            int displayOrder
    ) {
        return new QuietnessThreshold(
                1L,
                level,
                minimum == null ? null : new BigDecimal(minimum),
                maximum == null ? null : new BigDecimal(maximum),
                displayOrder
        );
    }
}

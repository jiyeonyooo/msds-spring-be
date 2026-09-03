package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.response.GuesthouseQuietnessSummaryResponse;
import com.example.meditation.quietness.dto.response.HourlyQuietnessResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceRecommendationResponse;
import com.example.meditation.quietness.dto.response.SpaceQuietnessResponse;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.entity.QuietSpace;
import com.example.meditation.quietness.entity.QuietSpaceType;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietSpaceRepository;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuietnessServiceTest {

    private final NoiseMeasurementRepository measurementRepository = mock(NoiseMeasurementRepository.class);
    private final QuietnessThresholdRepository thresholdRepository = mock(QuietnessThresholdRepository.class);
    private final QuietSpaceRepository spaceRepository = mock(QuietSpaceRepository.class);
    private final QuietnessService service =
            new QuietnessService(measurementRepository, thresholdRepository, spaceRepository);

    private final LocalDateTime measuredAt = LocalDateTime.of(2026, 8, 31, 15, 0);

    @BeforeEach
    void setUp() {
        when(thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(
                        threshold(QuietnessLevel.QUIET, "0.00", "39.99", 1),
                        threshold(QuietnessLevel.NORMAL, "40.00", "60.00", 2),
                        threshold(QuietnessLevel.LOUD, "60.01", null, 3)
                ));
        QuietSpace firstSpace = space(10L, "명상실", QuietSpaceType.MEDITATION_ROOM);
        QuietSpace secondSpace = space(20L, "1층 라운지", QuietSpaceType.LOUNGE);
        when(spaceRepository.findAllByGuesthouseIdOrderByIdAsc(1L))
                .thenReturn(List.of(firstSpace, secondSpace));
        when(spaceRepository.findById(10L)).thenReturn(java.util.Optional.of(firstSpace));
        when(spaceRepository.findById(20L)).thenReturn(java.util.Optional.of(secondSpace));
    }

    @Test
    void 공간별_최신값의_평균으로_숙소_종합지수를_계산한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        GuesthouseQuietnessSummaryResponse response = service.getGuesthouseSummary(1L);

        assertThat(response.averageDecibel()).isEqualByComparingTo("40.00");
        assertThat(response.level()).isEqualTo(QuietnessLevel.NORMAL);
        assertThat(response.measuredSpaceCount()).isEqualTo(2);
    }

    @Test
    void 공간별_현재_조용함_목록을_반환한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        List<SpaceQuietnessResponse> responses = service.getSpaces(1L);

        assertThat(responses).extracting(SpaceQuietnessResponse::spaceId).containsExactly(10L, 20L);
        assertThat(responses).extracting(SpaceQuietnessResponse::spaceName)
                .containsExactly("명상실", "1층 라운지");
        assertThat(responses).extracting(SpaceQuietnessResponse::level)
                .containsExactly(QuietnessLevel.QUIET, QuietnessLevel.NORMAL);
    }

    @Test
    void 데시벨이_가장_낮은_공간을_추천한다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(measurement(10L, "30.00"), measurement(20L, "50.00")));

        QuietSpaceRecommendationResponse response = service.recommendQuietSpace(1L);

        assertThat(response.spaceId()).isEqualTo(10L);
        assertThat(response.spaceName()).isEqualTo("명상실");
        assertThat(response.spaceType()).isEqualTo(QuietSpaceType.MEDITATION_ROOM);
        assertThat(response.decibel()).isEqualByComparingTo("30.00");
        assertThat(response.level()).isEqualTo(QuietnessLevel.QUIET);
    }

    @Test
    void 측정값이_없는_숙소는_조회할_수_없다() {
        when(measurementRepository.findLatestForEachSpaceByGuesthouseId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getGuesthouseSummary(1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 측정값을_시간단위로_묶어_평균과_최소최대를_계산한다() {
        LocalDateTime from = measuredAt.withMinute(0);
        LocalDateTime to = from.plusHours(2);
        when(measurementRepository
                .findAllByGuesthouseIdAndSpaceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        1L, 10L, from, to
                ))
                .thenReturn(List.of(
                        measurement(10L, "30.00", from.plusMinutes(5)),
                        measurement(10L, "50.00", from.plusMinutes(30)),
                        measurement(10L, "60.00", from.plusHours(1).plusMinutes(10))
                ));

        List<HourlyQuietnessResponse> responses =
                service.getHourlyStatistics(1L, 10L, from, to);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).averageDecibel()).isEqualByComparingTo("40.00");
        assertThat(responses.get(0).minimumDecibel()).isEqualByComparingTo("30.00");
        assertThat(responses.get(0).maximumDecibel()).isEqualByComparingTo("50.00");
        assertThat(responses.get(0).sampleCount()).isEqualTo(2);
        assertThat(responses.get(0).level()).isEqualTo(QuietnessLevel.NORMAL);
    }

    private NoiseMeasurement measurement(Long spaceId, String decibel) {
        return measurement(spaceId, decibel, measuredAt);
    }

    private NoiseMeasurement measurement(Long spaceId, String decibel, LocalDateTime time) {
        return new NoiseMeasurement(null, 1L, spaceId, new BigDecimal(decibel), time);
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

    private QuietSpace space(Long id, String name, QuietSpaceType type) {
        QuietSpace space = new QuietSpace(1L, name, type);
        ReflectionTestUtils.setField(space, "id", id);
        return space;
    }
}

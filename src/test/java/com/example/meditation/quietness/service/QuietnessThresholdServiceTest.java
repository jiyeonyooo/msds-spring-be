package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.QuietnessThresholdUpdateRequest;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuietnessThresholdServiceTest {

    private final QuietnessThresholdRepository repository = mock(QuietnessThresholdRepository.class);
    private final QuietnessThresholdService service = new QuietnessThresholdService(repository);

    @Test
    void 기준이_없으면_빈틈없는_다섯_단계_기본값을_생성한다() {
        when(repository.existsByGuesthouseId(1L)).thenReturn(false);

        service.initializeDefaultsIfMissing(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuietnessThreshold>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(QuietnessThreshold::getLevel)
                .containsExactly(
                        QuietnessLevel.VERY_QUIET,
                        QuietnessLevel.QUIET,
                        QuietnessLevel.NORMAL,
                        QuietnessLevel.LOUD,
                        QuietnessLevel.VERY_LOUD
                );
        assertThat(captor.getValue().get(0).includes(new java.math.BigDecimal("29.99"))).isTrue();
        assertThat(captor.getValue().get(1).includes(new java.math.BigDecimal("30.00"))).isTrue();
        assertThat(captor.getValue().get(4).includes(new java.math.BigDecimal("70.00"))).isTrue();
    }

    @Test
    void 기준이_이미_있으면_중복_생성하지_않는다() {
        when(repository.existsByGuesthouseId(1L)).thenReturn(true);

        service.initializeDefaultsIfMissing(1L);

        verify(repository, never()).saveAll(any());
    }

    @Test
    void 네_경계값을_수정하면_다섯_단계가_빈틈없이_갱신된다() {
        List<QuietnessThreshold> thresholds = List.of(
                threshold(QuietnessLevel.VERY_QUIET, null, "29.99", 1),
                threshold(QuietnessLevel.QUIET, "30.00", "39.99", 2),
                threshold(QuietnessLevel.NORMAL, "40.00", "54.99", 3),
                threshold(QuietnessLevel.LOUD, "55.00", "69.99", 4),
                threshold(QuietnessLevel.VERY_LOUD, "70.00", null, 5)
        );
        when(repository.existsByGuesthouseId(1L)).thenReturn(true);
        when(repository.findAllByGuesthouseIdOrderByDisplayOrderAsc(1L)).thenReturn(thresholds);

        var responses = service.updateThresholds(
                1L,
                new QuietnessThresholdUpdateRequest(
                        new java.math.BigDecimal("25.00"),
                        new java.math.BigDecimal("35.00"),
                        new java.math.BigDecimal("50.00"),
                        new java.math.BigDecimal("65.00")
                )
        );

        assertThat(responses).extracting(response -> response.minDecibel())
                .containsExactly(null, new java.math.BigDecimal("25.01"),
                        new java.math.BigDecimal("35.01"), new java.math.BigDecimal("50.01"),
                        new java.math.BigDecimal("65.01"));
        assertThat(responses).extracting(response -> response.maxDecibel())
                .containsExactly(new java.math.BigDecimal("25.00"),
                        new java.math.BigDecimal("35.00"), new java.math.BigDecimal("50.00"),
                        new java.math.BigDecimal("65.00"), null);
    }

    @Test
    void 경계값은_단계_순서대로_커야_한다() {
        var request = new QuietnessThresholdUpdateRequest(
                new java.math.BigDecimal("30.00"),
                new java.math.BigDecimal("30.00"),
                new java.math.BigDecimal("50.00"),
                new java.math.BigDecimal("60.00")
        );

        assertThatThrownBy(() -> service.updateThresholds(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단계 순서대로");
        verify(repository, never()).findAllByGuesthouseIdOrderByDisplayOrderAsc(1L);
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
                minimum == null ? null : new java.math.BigDecimal(minimum),
                maximum == null ? null : new java.math.BigDecimal(maximum),
                displayOrder
        );
    }
}

package com.example.meditation.quietness.service;

import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}

package com.example.meditation.quietness.service;

import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuietnessThresholdService {

    private final QuietnessThresholdRepository thresholdRepository;

    public void initializeDefaultsIfMissing(Long guesthouseId) {
        if (thresholdRepository.existsByGuesthouseId(guesthouseId)) {
            return;
        }

        thresholdRepository.saveAll(List.of(
                threshold(guesthouseId, QuietnessLevel.VERY_QUIET, null, "29.99", 1),
                threshold(guesthouseId, QuietnessLevel.QUIET, "30.00", "39.99", 2),
                threshold(guesthouseId, QuietnessLevel.NORMAL, "40.00", "54.99", 3),
                threshold(guesthouseId, QuietnessLevel.LOUD, "55.00", "69.99", 4),
                threshold(guesthouseId, QuietnessLevel.VERY_LOUD, "70.00", null, 5)
        ));
    }

    private QuietnessThreshold threshold(
            Long guesthouseId,
            QuietnessLevel level,
            String minimum,
            String maximum,
            int displayOrder
    ) {
        return new QuietnessThreshold(
                guesthouseId,
                level,
                minimum == null ? null : new BigDecimal(minimum),
                maximum == null ? null : new BigDecimal(maximum),
                displayOrder
        );
    }
}

package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietnessLevel;

import java.math.BigDecimal;

public record QuietnessThresholdResponse(
        Long thresholdId,
        Long guesthouseId,
        QuietnessLevel level,
        BigDecimal minDecibel,
        BigDecimal maxDecibel,
        Integer displayOrder
) {
}

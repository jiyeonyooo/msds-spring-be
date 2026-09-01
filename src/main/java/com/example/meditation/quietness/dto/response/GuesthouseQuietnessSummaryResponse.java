package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietnessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GuesthouseQuietnessSummaryResponse(
        Long guesthouseId,
        BigDecimal averageDecibel,
        QuietnessLevel level,
        int measuredSpaceCount,
        LocalDateTime latestMeasuredAt
) {
}

package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietnessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuietSpaceRecommendationResponse(
        Long guesthouseId,
        Long spaceId,
        BigDecimal decibel,
        QuietnessLevel level,
        LocalDateTime measuredAt
) {
}

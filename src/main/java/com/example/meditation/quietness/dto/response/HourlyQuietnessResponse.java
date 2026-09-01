package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietnessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HourlyQuietnessResponse(
        LocalDateTime hourStart,
        BigDecimal averageDecibel,
        BigDecimal minimumDecibel,
        BigDecimal maximumDecibel,
        QuietnessLevel level,
        long sampleCount
) {
}

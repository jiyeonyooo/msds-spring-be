package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietSpaceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SpaceQuietnessResponse(
        Long spaceId,
        String spaceName,
        QuietSpaceType spaceType,
        BigDecimal decibel,
        QuietnessLevel level,
        LocalDateTime measuredAt
) {
}

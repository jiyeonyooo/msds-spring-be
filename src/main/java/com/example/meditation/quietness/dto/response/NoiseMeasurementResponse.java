package com.example.meditation.quietness.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoiseMeasurementResponse(
        Long measurementId,
        Long deviceId,
        Long guesthouseId,
        Long spaceId,
        BigDecimal decibel,
        LocalDateTime measuredAt
) {
}

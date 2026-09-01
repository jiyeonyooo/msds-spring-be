package com.example.meditation.quietness.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoiseMeasurementCreateRequest(
        @NotNull Long deviceId,
        @NotNull @DecimalMin("0.0") BigDecimal decibel,
        LocalDateTime measuredAt
) {
    public LocalDateTime resolvedMeasuredAt() {
        return measuredAt == null ? LocalDateTime.now() : measuredAt;
    }
}

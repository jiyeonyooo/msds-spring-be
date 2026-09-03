package com.example.meditation.quietness.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoiseMeasurementCreateRequest(
        @NotNull Long deviceId,
        @NotNull @DecimalMin("0.0") BigDecimal decibel,
        @PastOrPresent(message = "측정 시각은 현재보다 미래일 수 없습니다.") LocalDateTime measuredAt
) {
    public LocalDateTime resolvedMeasuredAt() {
        return measuredAt == null ? LocalDateTime.now() : measuredAt;
    }
}

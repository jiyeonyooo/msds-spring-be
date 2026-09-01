package com.example.meditation.quietness.dto.request;

import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import jakarta.validation.constraints.NotNull;

public record NoiseDeviceStatusUpdateRequest(
        @NotNull NoiseDeviceStatus status
) {
}

package com.example.meditation.quietness.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoiseDeviceCreateRequest(
        @NotNull Long guesthouseId,
        @NotNull Long spaceId,
        @NotBlank @Size(max = 100) String deviceName,
        @NotBlank @Size(max = 100) String serialNumber,
        @Size(max = 100) String modelName
) {
}

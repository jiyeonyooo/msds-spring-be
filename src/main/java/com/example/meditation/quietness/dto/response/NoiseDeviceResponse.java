package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.NoiseDeviceStatus;

import java.time.LocalDateTime;

public record NoiseDeviceResponse(
        Long deviceId,
        Long guesthouseId,
        Long spaceId,
        String deviceName,
        String serialNumber,
        String modelName,
        NoiseDeviceStatus status,
        LocalDateTime installedAt,
        LocalDateTime lastConnectedAt
) {
}

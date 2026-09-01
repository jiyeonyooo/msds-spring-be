package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.NoiseDeviceCreateRequest;
import com.example.meditation.quietness.dto.request.NoiseDeviceStatusUpdateRequest;
import com.example.meditation.quietness.dto.request.NoiseMeasurementCreateRequest;
import com.example.meditation.quietness.dto.response.NoiseDeviceResponse;
import com.example.meditation.quietness.dto.response.NoiseMeasurementResponse;
import com.example.meditation.quietness.entity.NoiseDevice;
import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuietnessAdminService {

    private final NoiseDeviceRepository deviceRepository;
    private final NoiseMeasurementRepository measurementRepository;

    public List<NoiseDeviceResponse> getDevices(Long guesthouseId) {
        return deviceRepository.findAllByGuesthouseId(guesthouseId).stream()
                .map(this::toDeviceResponse)
                .toList();
    }

    @Transactional
    public NoiseDeviceResponse createDevice(NoiseDeviceCreateRequest request) {
        if (deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new IllegalArgumentException("이미 등록된 측정기기 일련번호입니다.");
        }

        NoiseDevice device = deviceRepository.save(new NoiseDevice(
                request.guesthouseId(),
                request.spaceId(),
                request.deviceName(),
                request.serialNumber(),
                request.modelName(),
                NoiseDeviceStatus.ACTIVE
        ));
        return toDeviceResponse(device);
    }

    @Transactional
    public NoiseDeviceResponse updateDeviceStatus(
            Long deviceId,
            NoiseDeviceStatusUpdateRequest request
    ) {
        NoiseDevice device = findDevice(deviceId);
        device.updateStatus(request.status());
        return toDeviceResponse(device);
    }

    @Transactional
    public NoiseMeasurementResponse createMeasurement(NoiseMeasurementCreateRequest request) {
        NoiseDevice device = findDevice(request.deviceId());
        if (device.getStatus() != NoiseDeviceStatus.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 측정기기만 측정값을 등록할 수 있습니다.");
        }

        LocalDateTime measuredAt = request.resolvedMeasuredAt();
        NoiseMeasurement measurement = measurementRepository.save(new NoiseMeasurement(
                device,
                device.getGuesthouseId(),
                device.getSpaceId(),
                request.decibel(),
                measuredAt
        ));
        device.markConnected(measuredAt);

        return new NoiseMeasurementResponse(
                measurement.getId(),
                device.getId(),
                device.getGuesthouseId(),
                device.getSpaceId(),
                measurement.getDecibel(),
                measurement.getMeasuredAt()
        );
    }

    private NoiseDevice findDevice(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "소음 측정기기를 찾을 수 없습니다."
                ));
    }

    private NoiseDeviceResponse toDeviceResponse(NoiseDevice device) {
        return new NoiseDeviceResponse(
                device.getId(),
                device.getGuesthouseId(),
                device.getSpaceId(),
                device.getDeviceName(),
                device.getSerialNumber(),
                device.getModelName(),
                device.getStatus(),
                device.getInstalledAt(),
                device.getLastConnectedAt()
        );
    }
}

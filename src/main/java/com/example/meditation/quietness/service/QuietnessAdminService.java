package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.NoiseDeviceCreateRequest;
import com.example.meditation.quietness.dto.request.NoiseDeviceStatusUpdateRequest;
import com.example.meditation.quietness.dto.request.NoiseMeasurementCreateRequest;
import com.example.meditation.quietness.dto.request.QuietSpaceCreateRequest;
import com.example.meditation.quietness.dto.request.QuietnessThresholdUpdateRequest;
import com.example.meditation.quietness.dto.response.NoiseDeviceResponse;
import com.example.meditation.quietness.dto.response.NoiseMeasurementResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceResponse;
import com.example.meditation.quietness.dto.response.QuietnessThresholdResponse;
import com.example.meditation.quietness.entity.NoiseDevice;
import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietSpace;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietSpaceRepository;
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
    private final QuietSpaceRepository spaceRepository;
    private final QuietnessThresholdService thresholdService;

    public List<QuietSpaceResponse> getSpaces(Long guesthouseId) {
        return spaceRepository.findAllByGuesthouseIdOrderByIdAsc(guesthouseId).stream()
                .map(this::toSpaceResponse)
                .toList();
    }

    @Transactional
    public QuietSpaceResponse createSpace(QuietSpaceCreateRequest request) {
        if (spaceRepository.existsByGuesthouseIdAndName(request.guesthouseId(), request.name())) {
            throw new IllegalArgumentException("해당 숙소에 같은 이름의 공간이 이미 있습니다.");
        }

        QuietSpace space = spaceRepository.save(new QuietSpace(
                request.guesthouseId(),
                request.name(),
                request.type()
        ));
        thresholdService.initializeDefaultsIfMissing(request.guesthouseId());
        return toSpaceResponse(space);
    }

    public List<NoiseDeviceResponse> getDevices(Long guesthouseId) {
        return deviceRepository.findAllByGuesthouseId(guesthouseId).stream()
                .map(this::toDeviceResponse)
                .toList();
    }

    @Transactional
    public List<QuietnessThresholdResponse> getThresholds(Long guesthouseId) {
        return thresholdService.getThresholds(guesthouseId);
    }

    @Transactional
    public List<QuietnessThresholdResponse> updateThresholds(
            Long guesthouseId,
            QuietnessThresholdUpdateRequest request
    ) {
        return thresholdService.updateThresholds(guesthouseId, request);
    }

    @Transactional
    public NoiseDeviceResponse createDevice(NoiseDeviceCreateRequest request) {
        if (deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new IllegalArgumentException("이미 등록된 측정기기 일련번호입니다.");
        }

        QuietSpace space = findSpace(request.spaceId());
        if (!space.getGuesthouseId().equals(request.guesthouseId())) {
            throw new IllegalArgumentException("측정 공간이 해당 숙소에 속하지 않습니다.");
        }

        NoiseDevice device = deviceRepository.save(new NoiseDevice(
                request.guesthouseId(),
                request.spaceId(),
                request.deviceName(),
                request.serialNumber(),
                request.modelName(),
                NoiseDeviceStatus.ACTIVE
        ));
        thresholdService.initializeDefaultsIfMissing(request.guesthouseId());
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

    private QuietSpace findSpace(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "조용함 측정 공간을 찾을 수 없습니다."
                ));
    }

    private QuietSpaceResponse toSpaceResponse(QuietSpace space) {
        return new QuietSpaceResponse(
                space.getId(),
                space.getGuesthouseId(),
                space.getName(),
                space.getType(),
                space.isActive()
        );
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

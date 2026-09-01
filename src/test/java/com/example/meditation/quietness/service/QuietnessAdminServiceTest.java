package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.NoiseDeviceCreateRequest;
import com.example.meditation.quietness.dto.request.NoiseMeasurementCreateRequest;
import com.example.meditation.quietness.dto.response.NoiseMeasurementResponse;
import com.example.meditation.quietness.entity.NoiseDevice;
import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuietnessAdminServiceTest {

    private final NoiseDeviceRepository deviceRepository = mock(NoiseDeviceRepository.class);
    private final NoiseMeasurementRepository measurementRepository = mock(NoiseMeasurementRepository.class);
    private final QuietnessAdminService service =
            new QuietnessAdminService(deviceRepository, measurementRepository);

    @Test
    void 중복된_일련번호의_기기는_등록할_수_없다() {
        NoiseDeviceCreateRequest request = new NoiseDeviceCreateRequest(
                1L, 10L, "측정기", "SERIAL-1", "MODEL-A"
        );
        when(deviceRepository.existsBySerialNumber("SERIAL-1")).thenReturn(true);

        assertThatThrownBy(() -> service.createDevice(request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(deviceRepository, never()).save(any(NoiseDevice.class));
    }

    @Test
    void 활성_기기의_측정값을_저장하고_마지막_연결시각을_갱신한다() {
        NoiseDevice device = device(NoiseDeviceStatus.ACTIVE);
        LocalDateTime measuredAt = LocalDateTime.of(2026, 9, 1, 10, 30);
        when(deviceRepository.findById(5L)).thenReturn(Optional.of(device));
        when(measurementRepository.save(any(NoiseMeasurement.class))).thenAnswer(invocation -> {
            NoiseMeasurement measurement = invocation.getArgument(0);
            ReflectionTestUtils.setField(measurement, "id", 100L);
            return measurement;
        });

        NoiseMeasurementResponse response = service.createMeasurement(
                new NoiseMeasurementCreateRequest(5L, new BigDecimal("38.50"), measuredAt)
        );

        assertThat(response.measurementId()).isEqualTo(100L);
        assertThat(response.spaceId()).isEqualTo(10L);
        assertThat(device.getLastConnectedAt()).isEqualTo(measuredAt);
    }

    @Test
    void 비활성_기기의_측정값은_등록할_수_없다() {
        NoiseDevice device = device(NoiseDeviceStatus.INACTIVE);
        when(deviceRepository.findById(5L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.createMeasurement(
                new NoiseMeasurementCreateRequest(5L, new BigDecimal("38.50"), null)
        )).isInstanceOf(IllegalArgumentException.class);
        verify(measurementRepository, never()).save(any(NoiseMeasurement.class));
    }

    private NoiseDevice device(NoiseDeviceStatus status) {
        NoiseDevice device = new NoiseDevice(1L, 10L, "측정기", "SERIAL-1", "MODEL-A", status);
        ReflectionTestUtils.setField(device, "id", 5L);
        return device;
    }
}

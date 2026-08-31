package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.NoiseDevice;
import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoiseMeasurementRepositoryTest {

    @Autowired
    private NoiseDeviceRepository deviceRepository;

    @Autowired
    private NoiseMeasurementRepository measurementRepository;

    @Test
    void 숙소의_각_공간별_가장_최근_측정값을_조회한다() {
        NoiseDevice firstDevice = deviceRepository.save(device(10L, "SERIAL-10"));
        NoiseDevice secondDevice = deviceRepository.save(device(20L, "SERIAL-20"));
        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 31, 14, 0);

        measurementRepository.saveAll(List.of(
                measurement(firstDevice, 10L, "45.00", baseTime),
                measurement(firstDevice, 10L, "30.00", baseTime.plusMinutes(10)),
                measurement(secondDevice, 20L, "55.00", baseTime),
                measurement(secondDevice, 20L, "50.00", baseTime.plusMinutes(10))
        ));

        List<NoiseMeasurement> measurements =
                measurementRepository.findLatestForEachSpaceByGuesthouseId(1L);

        assertThat(measurements).hasSize(2);
        assertThat(measurements).extracting(NoiseMeasurement::getSpaceId).containsExactly(10L, 20L);
        assertThat(measurements).extracting(NoiseMeasurement::getDecibel)
                .containsExactly(new BigDecimal("30.00"), new BigDecimal("50.00"));
    }

    private NoiseDevice device(Long spaceId, String serialNumber) {
        return new NoiseDevice(
                1L,
                spaceId,
                "측정기 " + spaceId,
                serialNumber,
                "MODEL-A",
                NoiseDeviceStatus.ACTIVE
        );
    }

    private NoiseMeasurement measurement(
            NoiseDevice device,
            Long spaceId,
            String decibel,
            LocalDateTime measuredAt
    ) {
        return new NoiseMeasurement(
                device,
                1L,
                spaceId,
                new BigDecimal(decibel),
                measuredAt
        );
    }
}

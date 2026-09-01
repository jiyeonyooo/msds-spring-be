package com.example.meditation.quietness.config;

import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietSpace;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietSpaceRepository;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import com.example.meditation.quietness.service.QuietnessThresholdService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuietnessDemoDataInitializerTest {

    @Autowired
    private QuietSpaceRepository spaceRepository;

    @Autowired
    private NoiseDeviceRepository deviceRepository;

    @Autowired
    private NoiseMeasurementRepository measurementRepository;

    @Autowired
    private QuietnessThresholdRepository thresholdRepository;

    @Test
    void 활성화하면_데모_공간과_기기와_시간대별_측정값을_중복없이_생성한다() {
        QuietnessDemoDataInitializer initializer = new QuietnessDemoDataInitializer(
                spaceRepository,
                deviceRepository,
                measurementRepository,
                new QuietnessThresholdService(thresholdRepository),
                1L
        );

        initializer.run(new DefaultApplicationArguments());
        initializer.run(new DefaultApplicationArguments());

        assertThat(spaceRepository.findAllByGuesthouseIdOrderByIdAsc(1L))
                .extracting(QuietSpace::getName)
                .containsExactly("마음쉼 명상실", "1층 라운지", "정원 휴게공간");
        assertThat(deviceRepository.findAllByGuesthouseId(1L)).hasSize(3);
        assertThat(measurementRepository.findAll())
                .hasSize(18)
                .extracting(NoiseMeasurement::getGuesthouseId)
                .containsOnly(1L);
        assertThat(thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(1L))
                .hasSize(5);
    }
}

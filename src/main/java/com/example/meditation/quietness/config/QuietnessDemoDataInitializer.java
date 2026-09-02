package com.example.meditation.quietness.config;

import com.example.meditation.quietness.entity.NoiseDevice;
import com.example.meditation.quietness.entity.NoiseDeviceStatus;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietSpace;
import com.example.meditation.quietness.entity.QuietSpaceType;
import com.example.meditation.quietness.repository.NoiseDeviceRepository;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietSpaceRepository;
import com.example.meditation.quietness.service.QuietnessThresholdService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.quietness.demo", name = "enabled", havingValue = "true")
public class QuietnessDemoDataInitializer implements ApplicationRunner {

    private final QuietSpaceRepository spaceRepository;
    private final NoiseDeviceRepository deviceRepository;
    private final NoiseMeasurementRepository measurementRepository;
    private final QuietnessThresholdService thresholdService;
    private final Long guesthouseId;

    public QuietnessDemoDataInitializer(
            QuietSpaceRepository spaceRepository,
            NoiseDeviceRepository deviceRepository,
            NoiseMeasurementRepository measurementRepository,
            QuietnessThresholdService thresholdService,
            @Value("${app.quietness.demo.guesthouse-id:1}") Long guesthouseId
    ) {
        this.spaceRepository = spaceRepository;
        this.deviceRepository = deviceRepository;
        this.measurementRepository = measurementRepository;
        this.thresholdService = thresholdService;
        this.guesthouseId = guesthouseId;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!spaceRepository.findAllByGuesthouseIdOrderByIdAsc(guesthouseId).isEmpty()) {
            return;
        }

        thresholdService.initializeDefaultsIfMissing(guesthouseId);
        List<QuietSpace> spaces = spaceRepository.saveAll(List.of(
                new QuietSpace(guesthouseId, "마음쉼 명상실", QuietSpaceType.MEDITATION_ROOM),
                new QuietSpace(guesthouseId, "1층 라운지", QuietSpaceType.LOUNGE),
                new QuietSpace(guesthouseId, "정원 휴게공간", QuietSpaceType.COMMON_AREA)
        ));

        List<NoiseDevice> devices = deviceRepository.saveAll(List.of(
                device(spaces.get(0), "MEDITATION"),
                device(spaces.get(1), "LOUNGE"),
                device(spaces.get(2), "GARDEN")
        ));

        LocalDateTime latestHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        List<NoiseMeasurement> measurements = new ArrayList<>();
        addMeasurements(measurements, devices.get(0), latestHour, "31.20", "33.10", "35.40", "32.80", "30.50", "34.20");
        addMeasurements(measurements, devices.get(1), latestHour, "45.10", "48.30", "51.20", "47.80", "43.90", "46.40");
        addMeasurements(measurements, devices.get(2), latestHour, "39.20", "41.70", "44.10", "42.30", "38.60", "40.80");
        measurementRepository.saveAll(measurements);

        devices.forEach(device -> device.markConnected(latestHour));
    }

    private NoiseDevice device(QuietSpace space, String serialSuffix) {
        return new NoiseDevice(
                guesthouseId,
                space.getId(),
                space.getName() + " 소음계",
                "QUIET-DEMO-" + guesthouseId + "-" + serialSuffix,
                "MSDS-DEMO-1",
                NoiseDeviceStatus.ACTIVE
        );
    }

    private void addMeasurements(
            List<NoiseMeasurement> measurements,
            NoiseDevice device,
            LocalDateTime latestHour,
            String... decibels
    ) {
        for (int index = 0; index < decibels.length; index++) {
            LocalDateTime measuredAt = latestHour
                    .minusHours(decibels.length - 1L - index)
                    .plusMinutes(30);
            measurements.add(new NoiseMeasurement(
                    device,
                    guesthouseId,
                    device.getSpaceId(),
                    new BigDecimal(decibels[index]),
                    measuredAt
            ));
        }
    }
}

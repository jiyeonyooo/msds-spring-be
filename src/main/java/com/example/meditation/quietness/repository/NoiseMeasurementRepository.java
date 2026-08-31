package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.NoiseMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoiseMeasurementRepository extends JpaRepository<NoiseMeasurement, Long> {
    Optional<NoiseMeasurement> findTopBySpaceIdOrderByMeasuredAtDesc(Long spaceId);

    List<NoiseMeasurement> findAllBySpaceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long spaceId,
            LocalDateTime from,
            LocalDateTime to
    );
}

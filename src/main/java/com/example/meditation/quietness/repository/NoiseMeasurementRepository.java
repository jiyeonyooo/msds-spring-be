package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.NoiseMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<NoiseMeasurement> findAllByGuesthouseIdAndSpaceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long guesthouseId,
            Long spaceId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
            select measurement
            from NoiseMeasurement measurement
            where measurement.guesthouseId = :guesthouseId
              and measurement.measuredAt = (
                  select max(candidate.measuredAt)
                  from NoiseMeasurement candidate
                  where candidate.guesthouseId = measurement.guesthouseId
                    and candidate.spaceId = measurement.spaceId
              )
            order by measurement.spaceId asc
            """)
    List<NoiseMeasurement> findLatestForEachSpaceByGuesthouseId(
            @Param("guesthouseId") Long guesthouseId
    );
}

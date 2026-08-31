package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.response.QuietnessHistoryPointResponse;
import com.example.meditation.quietness.dto.response.SpaceQuietnessResponse;
import com.example.meditation.quietness.entity.NoiseMeasurement;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.repository.NoiseMeasurementRepository;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuietnessService {

    private final NoiseMeasurementRepository measurementRepository;
    private final QuietnessThresholdRepository thresholdRepository;

    public SpaceQuietnessResponse getCurrentQuietness(Long guesthouseId, Long spaceId) {
        NoiseMeasurement measurement = measurementRepository.findTopBySpaceIdOrderByMeasuredAtDesc(spaceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 공간의 소음 측정값이 없습니다."
                ));

        if (!measurement.getGuesthouseId().equals(guesthouseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 숙소의 공간이 아닙니다.");
        }

        return new SpaceQuietnessResponse(
                spaceId,
                measurement.getDecibel(),
                resolveLevel(guesthouseId, measurement.getDecibel()),
                measurement.getMeasuredAt()
        );
    }

    public List<QuietnessHistoryPointResponse> getHistory(
            Long spaceId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작 시각은 종료 시각보다 늦을 수 없습니다.");
        }

        return measurementRepository
                .findAllBySpaceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(spaceId, from, to)
                .stream()
                .map(measurement -> new QuietnessHistoryPointResponse(
                        measurement.getDecibel(),
                        measurement.getMeasuredAt()
                ))
                .toList();
    }

    private QuietnessLevel resolveLevel(Long guesthouseId, BigDecimal decibel) {
        return thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(guesthouseId).stream()
                .filter(threshold -> threshold.includes(decibel))
                .map(threshold -> threshold.getLevel())
                .findFirst()
                .orElse(QuietnessLevel.UNKNOWN);
    }
}

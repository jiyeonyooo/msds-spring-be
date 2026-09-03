package com.example.meditation.quietness.service;

import com.example.meditation.quietness.dto.request.QuietnessThresholdUpdateRequest;
import com.example.meditation.quietness.dto.response.QuietnessThresholdResponse;
import com.example.meditation.quietness.entity.QuietnessLevel;
import com.example.meditation.quietness.entity.QuietnessThreshold;
import com.example.meditation.quietness.repository.QuietnessThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuietnessThresholdService {

    private static final BigDecimal STEP = new BigDecimal("0.01");

    private final QuietnessThresholdRepository thresholdRepository;

    @Transactional
    public void initializeDefaultsIfMissing(Long guesthouseId) {
        if (thresholdRepository.existsByGuesthouseId(guesthouseId)) {
            return;
        }

        thresholdRepository.saveAll(List.of(
                threshold(guesthouseId, QuietnessLevel.VERY_QUIET, null, "29.99", 1),
                threshold(guesthouseId, QuietnessLevel.QUIET, "30.00", "39.99", 2),
                threshold(guesthouseId, QuietnessLevel.NORMAL, "40.00", "54.99", 3),
                threshold(guesthouseId, QuietnessLevel.LOUD, "55.00", "69.99", 4),
                threshold(guesthouseId, QuietnessLevel.VERY_LOUD, "70.00", null, 5)
        ));
    }

    @Transactional
    public List<QuietnessThresholdResponse> getThresholds(Long guesthouseId) {
        initializeDefaultsIfMissing(guesthouseId);
        return thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(guesthouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<QuietnessThresholdResponse> updateThresholds(
            Long guesthouseId,
            QuietnessThresholdUpdateRequest request
    ) {
        validateAscending(request);
        initializeDefaultsIfMissing(guesthouseId);

        List<QuietnessThreshold> thresholds =
                thresholdRepository.findAllByGuesthouseIdOrderByDisplayOrderAsc(guesthouseId);
        if (thresholds.size() != 5) {
            throw new IllegalStateException("조용함 기준값은 다섯 단계로 구성되어야 합니다.");
        }

        update(thresholds, QuietnessLevel.VERY_QUIET, null, request.veryQuietMax());
        update(thresholds, QuietnessLevel.QUIET, next(request.veryQuietMax()), request.quietMax());
        update(thresholds, QuietnessLevel.NORMAL, next(request.quietMax()), request.normalMax());
        update(thresholds, QuietnessLevel.LOUD, next(request.normalMax()), request.loudMax());
        update(thresholds, QuietnessLevel.VERY_LOUD, next(request.loudMax()), null);

        return thresholds.stream().map(this::toResponse).toList();
    }

    private void validateAscending(QuietnessThresholdUpdateRequest request) {
        if (request.veryQuietMax().compareTo(request.quietMax()) >= 0
                || request.quietMax().compareTo(request.normalMax()) >= 0
                || request.normalMax().compareTo(request.loudMax()) >= 0) {
            throw new IllegalArgumentException("조용함 기준값은 단계 순서대로 커져야 합니다.");
        }
    }

    private void update(
            List<QuietnessThreshold> thresholds,
            QuietnessLevel level,
            BigDecimal minimum,
            BigDecimal maximum
    ) {
        QuietnessThreshold threshold = thresholds.stream()
                .filter(candidate -> candidate.getLevel() == level)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("조용함 기준값 단계가 올바르지 않습니다."));
        threshold.updateRange(minimum, maximum);
    }

    private BigDecimal next(BigDecimal value) {
        return value.add(STEP);
    }

    private QuietnessThresholdResponse toResponse(QuietnessThreshold threshold) {
        return new QuietnessThresholdResponse(
                threshold.getId(),
                threshold.getGuesthouseId(),
                threshold.getLevel(),
                threshold.getMinDecibel(),
                threshold.getMaxDecibel(),
                threshold.getDisplayOrder()
        );
    }

    private QuietnessThreshold threshold(
            Long guesthouseId,
            QuietnessLevel level,
            String minimum,
            String maximum,
            int displayOrder
    ) {
        return new QuietnessThreshold(
                guesthouseId,
                level,
                minimum == null ? null : new BigDecimal(minimum),
                maximum == null ? null : new BigDecimal(maximum),
                displayOrder
        );
    }
}

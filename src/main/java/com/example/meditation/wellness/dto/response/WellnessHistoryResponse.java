package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.WellnessLevel;

import java.time.LocalDateTime;

public record WellnessHistoryResponse(
        Long checkId,
        int totalScore,
        WellnessLevel level,
        StayStage stayStage,
        LocalDateTime checkedAt
) {
}

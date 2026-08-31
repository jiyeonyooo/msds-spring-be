package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.WellnessLevel;

public record WellnessCheckResultResponse(
        Long checkId,
        int totalScore,
        WellnessLevel level,
        String levelLabel,
        String message,
        boolean saved
) {
}

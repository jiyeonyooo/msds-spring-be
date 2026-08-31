package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.WellnessLevel;

import java.time.LocalDateTime;

public record WellnessTrendPointResponse(
        Long checkId,
        int totalScore,
        WellnessLevel level,
        LocalDateTime checkedAt
) {
}

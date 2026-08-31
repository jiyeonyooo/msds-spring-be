package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.WellnessLevel;

import java.time.LocalDateTime;
import java.util.List;

public record WellnessCheckDetailResponse(
        Long checkId,
        Long reservationId,
        int totalScore,
        WellnessLevel level,
        String levelLabel,
        String message,
        StayStage stayStage,
        LocalDateTime checkedAt,
        List<WellnessAnswerDetailResponse> answers
) {
}

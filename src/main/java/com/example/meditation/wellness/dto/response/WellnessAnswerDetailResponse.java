package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.WellnessCategory;

public record WellnessAnswerDetailResponse(
        Long questionId,
        WellnessCategory category,
        String content,
        int answerValue,
        int convertedValue
) {
}

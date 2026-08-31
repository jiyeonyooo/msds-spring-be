package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.WellnessCategory;

import java.util.List;

public record WellnessQuestionResponse(
        Long questionId,
        WellnessCategory category,
        String content,
        int displayOrder,
        List<WellnessQuestionOptionResponse> options
) {
}

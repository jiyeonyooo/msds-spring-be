package com.example.meditation.wellness.dto.request;

import com.example.meditation.wellness.entity.StayStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WellnessCheckRequest(
        Long reservationId,
        StayStage stayStage,
        @NotEmpty List<@Valid WellnessAnswerRequest> answers
) {
    public StayStage resolvedStayStage() {
        return stayStage == null ? StayStage.GENERAL : stayStage;
    }
}

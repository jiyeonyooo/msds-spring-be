package com.example.meditation.wellness.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WellnessAnswerRequest(
        @NotNull Long questionId,
        @NotNull @Min(0) @Max(4) Integer value
) {
}

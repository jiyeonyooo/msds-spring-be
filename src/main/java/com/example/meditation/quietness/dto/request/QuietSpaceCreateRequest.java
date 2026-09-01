package com.example.meditation.quietness.dto.request;

import com.example.meditation.quietness.entity.QuietSpaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuietSpaceCreateRequest(
        @NotNull Long guesthouseId,
        @NotBlank @Size(max = 100) String name,
        @NotNull QuietSpaceType type
) {
}

package meditation_program.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(@NotNull Long programReservationId, @NotBlank String content) {}


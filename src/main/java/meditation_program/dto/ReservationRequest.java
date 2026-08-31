package meditation_program.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(@NotNull Long programId, @Min(1) int quantity) {}

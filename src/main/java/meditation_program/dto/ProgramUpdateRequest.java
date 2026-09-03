package meditation_program.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProgramUpdateRequest(
        @NotBlank String name,
        String pictureUrl,
        @Min(1) int capacity
) {
}

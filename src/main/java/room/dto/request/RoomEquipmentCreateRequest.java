package room.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomEquipmentCreateRequest(
        @NotNull Long equipmentId,
        @NotNull @Min(1) Integer quantity,
        @Size(max = 255) String note
) {
}

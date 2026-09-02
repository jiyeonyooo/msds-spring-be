package room.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoomEquipmentsUpdateRequest(
        @NotNull List<@Valid RoomEquipmentCreateRequest> equipments
) {
}

package room.dto.response;

import room.entity.enums.EquipmentCategory;

public record RoomEquipmentOptionResponse(
        Long equipmentId,
        String name,
        EquipmentCategory category,
        String description,
        String iconUrl
) {
}

package room.dto.response;

public record EquipmentResponse(
        Long equipmentId,
        String name,
        Integer quantity,
        String note,
        String iconUrl
) {}

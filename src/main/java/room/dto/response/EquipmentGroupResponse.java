package room.dto.response;

import room.entity.enums.EquipmentCategory;

import java.util.List;

public record EquipmentGroupResponse(
        EquipmentCategory category,
        String categoryName,
        List<EquipmentResponse> equipments
) {}

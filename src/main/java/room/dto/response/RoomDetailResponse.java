package room.dto.response;

import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;

import java.util.List;

public record RoomDetailResponse(
        Long roomId,
        String name,
        String description,
        RoomType roomType,
        RoomStatus status,
        CapacityResponse capacity,
        RoomSpecsResponse roomSpecs,
        Integer basePrice,
        List<RoomImageResponse> images,
        List<EquipmentGroupResponse> equipmentGroups
) {}

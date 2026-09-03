package room.dto.response;

import room.entity.enums.RoomImageType;

public record RoomImageResponse(
        Long imageId,
        String imageUrl,
        RoomImageType imageType,
        Integer sortOrder
) {}

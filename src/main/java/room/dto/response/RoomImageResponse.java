package room.dto.response;

import room.dto.type.RoomImageType;

public record RoomImageResponse(
        Long imageId,
        String imageUrl,
        RoomImageType imageType,
        Integer sortOrder
) {}

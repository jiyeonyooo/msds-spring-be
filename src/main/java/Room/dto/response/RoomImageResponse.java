package Room.dto.response;

import Room.dto.type.RoomImageType;

public record RoomImageResponse(
        Long imageId,
        String imageUrl,
        RoomImageType imageType,
        Integer sortOrder
) {}

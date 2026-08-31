package room.dto.request;

import room.dto.type.RoomImageType;

public record RoomImageCreateRequest(
        String imageUrl,
        RoomImageType imageType,
        Integer sortOrder
) {}

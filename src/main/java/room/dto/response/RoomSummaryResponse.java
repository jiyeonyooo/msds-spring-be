package room.dto.response;

import room.entity.enums.RoomType;

import java.math.BigDecimal;

public record RoomSummaryResponse(
        Long roomId,
        String name,
        String description,
        String mainImageUrl,
        RoomType roomType,
        Integer standardGuests,
        Integer maxGuests,
        BigDecimal areaM2,
        Integer basePrice
) {}

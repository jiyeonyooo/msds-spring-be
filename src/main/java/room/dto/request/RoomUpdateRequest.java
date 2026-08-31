package room.dto.request;

import room.dto.type.BedType;
import room.dto.type.RoomStatus;
import room.dto.type.RoomType;
import room.dto.type.ViewType;

import java.math.BigDecimal;

public record RoomUpdateRequest(
        String name,
        String description,
        RoomType roomType,
        RoomStatus status,
        Integer standardGuests,
        Integer maxGuests,
        BigDecimal areaM2,
        Integer basePrice,
        BedType bedType,
        Integer bedCount,
        ViewType viewType
) {}

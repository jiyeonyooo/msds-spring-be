package room.dto.request;

import room.dto.type.BedType;
import room.dto.type.RoomType;
import room.dto.type.ViewType;

import java.math.BigDecimal;
import java.util.List;

public record RoomCreateRequest(
        String name,
        String description,
        RoomType roomType,
        Integer standardGuests,
        Integer maxGuests,
        BigDecimal areaM2,
        Integer basePrice,
        BedType bedType,
        Integer bedCount,
        ViewType viewType,
        List<RoomEquipmentCreateRequest> equipments
) {}

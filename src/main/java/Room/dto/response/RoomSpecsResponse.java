package Room.dto.response;

import Room.dto.type.ViewType;
import Room.entity.enums.BedType;

import java.math.BigDecimal;

public record RoomSpecsResponse(
        BigDecimal areaM2,
        BedType bedType,
        Integer bedCount,
        ViewType viewType
) {}

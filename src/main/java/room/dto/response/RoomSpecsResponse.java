package room.dto.response;

import room.dto.type.ViewType;
import room.entity.enums.BedType;

import java.math.BigDecimal;

public record RoomSpecsResponse(
        BigDecimal areaM2,
        BedType bedType,
        Integer bedCount,
        ViewType viewType
) {}

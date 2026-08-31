package room.dto.request;

import java.util.List;

public record RoomEquipmentsUpdateRequest(List<RoomEquipmentCreateRequest> equipments) {}

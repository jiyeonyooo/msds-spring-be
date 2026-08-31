package room.service;

import room.dto.response.CapacityResponse;
import room.dto.response.EquipmentGroupResponse;
import room.dto.response.EquipmentResponse;
import room.dto.response.RoomDetailResponse;
import room.dto.response.RoomSpecsResponse;
import room.dto.response.RoomSummaryResponse;
import room.entity.Room;
import room.entity.RoomEquipment;
import room.entity.RoomEquipmentMapping;
import room.entity.enums.EquipmentCategory;
import room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomSummaryResponse> getRooms() {
        return roomRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public RoomDetailResponse getRoom(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found: " + roomId
                ));

        return toDetailResponse(room);
    }

    private RoomSummaryResponse toSummaryResponse(Room room) {
        return new RoomSummaryResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getMainImageUrl(),
                room.getRoomType(),
                room.getStandardGuests(),
                room.getMaxGuests(),
                room.getAreaM2(),
                room.getBasePrice()
        );
    }

    private RoomDetailResponse toDetailResponse(Room room) {
        return new RoomDetailResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getRoomType(),
                room.getStatus(),
                new CapacityResponse(room.getStandardGuests(), room.getMaxGuests()),
                new RoomSpecsResponse(room.getAreaM2(), room.getBedType(), room.getBedCount(), null),
                room.getBasePrice(),
                List.of(),
                toEquipmentGroups(room.getEquipmentMappings())
        );
    }

    private List<EquipmentGroupResponse> toEquipmentGroups(
            List<RoomEquipmentMapping> mappings
    ) {
        Map<EquipmentCategory, List<EquipmentResponse>> grouped = new LinkedHashMap<>();

        for (RoomEquipmentMapping mapping : mappings) {
            RoomEquipment equipment = mapping.getEquipment();
            EquipmentResponse response = new EquipmentResponse(
                    equipment.getId(),
                    equipment.getName(),
                    mapping.getQuantity(),
                    mapping.getNote(),
                    equipment.getIconUrl()
            );

            grouped.computeIfAbsent(equipment.getCategory(), ignored -> new ArrayList<>())
                    .add(response);
        }

        return grouped.entrySet().stream()
                .map(entry -> new EquipmentGroupResponse(
                        entry.getKey(),
                        entry.getKey().name(),
                        List.copyOf(entry.getValue())
                ))
                .toList();
    }
}

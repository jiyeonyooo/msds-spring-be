package room.service;

import room.dto.response.CapacityResponse;
import room.dto.response.EquipmentGroupResponse;
import room.dto.response.EquipmentResponse;
import room.dto.response.RoomDetailResponse;
import room.dto.response.RoomSpecsResponse;
import room.dto.response.RoomSummaryResponse;
import room.dto.response.RoomImageResponse;
import room.dto.response.RoomEquipmentOptionResponse;
import room.dto.request.RoomCreateRequest;
import room.dto.request.RoomEquipmentsUpdateRequest;
import room.dto.request.RoomUpdateRequest;
import room.dto.request.RoomImageCreateRequest;
import room.entity.Room;
import room.entity.RoomImage;
import room.entity.RoomEquipment;
import room.entity.RoomEquipmentMapping;
import room.entity.enums.EquipmentCategory;
import room.entity.enums.RoomImageType;
import room.repository.RoomRepository;
import room.repository.RoomEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;

    public List<RoomSummaryResponse> getRooms() {
        return roomRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public RoomDetailResponse getRoom(Long roomId) {
        Room room = findRoomDetail(roomId);

        return toDetailResponse(room);
    }

    public List<RoomDetailResponse> getRoomsForAdmin() {
        return roomRepository.findAll().stream()
                .map(this::toDetailResponse)
                .toList();
    }

    public List<RoomEquipmentOptionResponse> getEquipmentOptions() {
        return roomEquipmentRepository.findAllByActiveTrueOrderByCategoryAscNameAsc().stream()
                .map(equipment -> new RoomEquipmentOptionResponse(
                        equipment.getId(),
                        equipment.getName(),
                        equipment.getCategory(),
                        equipment.getDescription(),
                        equipment.getIconUrl()
                ))
                .toList();
    }

    @Transactional
    public RoomDetailResponse createRoom(RoomCreateRequest request) {
        Room room = Room.create(
                request.name(),
                request.description(),
                request.roomType(),
                request.status(),
                request.minGuest(),
                request.maxGuest(),
                request.area(),
                request.basePrice(),
                request.mainImageUrl(),
                request.bedType(),
                request.bedCount()
        );

        return toDetailResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomDetailResponse updateRoom(Long roomId, RoomUpdateRequest request) {
        Room room = findRoomDetail(roomId);

        int minGuest = request.minGuest() == null
                ? room.getStandardGuests()
                : request.minGuest();
        int maxGuest = request.maxGuest() == null
                ? room.getMaxGuests()
                : request.maxGuest();

        if (minGuest > maxGuest) {
            throw new IllegalArgumentException(
                    "최대 숙박 인원은 최소 숙박 인원 이상이어야 합니다."
            );
        }

        room.update(
                request.name(),
                request.description(),
                request.roomType(),
                request.status(),
                request.minGuest(),
                request.maxGuest(),
                request.area(),
                request.basePrice(),
                request.mainImageUrl(),
                request.bedType(),
                request.bedCount()
        );

        return toDetailResponse(room);
    }

    @Transactional
    public RoomDetailResponse updateRoomEquipments(
            Long roomId,
            RoomEquipmentsUpdateRequest request
    ) {
        Room room = findRoomDetail(roomId);
        Set<Long> equipmentIds = request.equipments().stream()
                .map(item -> item.equipmentId())
                .collect(Collectors.toCollection(HashSet::new));
        if (equipmentIds.size() != request.equipments().size()) {
            throw new IllegalArgumentException("같은 비품을 중복해서 등록할 수 없습니다.");
        }

        Map<Long, RoomEquipment> equipments = roomEquipmentRepository.findAllById(equipmentIds)
                .stream()
                .filter(RoomEquipment::getActive)
                .collect(Collectors.toMap(RoomEquipment::getId, Function.identity()));
        if (equipments.size() != equipmentIds.size()) {
            throw new IllegalArgumentException("존재하지 않거나 비활성화된 객실 비품이 포함되어 있습니다.");
        }

        List<RoomEquipmentMapping> mappings = request.equipments().stream()
                .map(item -> RoomEquipmentMapping.create(
                        room,
                        equipments.get(item.equipmentId()),
                        item.quantity(),
                        item.note()
                ))
                .toList();
        room.replaceEquipmentMappings(mappings);
        return toDetailResponse(room);
    }

    @Transactional
    public List<RoomImageResponse> addRoomImages(
            Long roomId,
            List<RoomImageCreateRequest> requests
    ) {
        Room room = findRoomDetail(roomId);

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one room image is required.");
        }
        boolean hasPersistedMainImage = room.getImages().stream()
                .anyMatch(image -> image.getImageType() == RoomImageType.MAIN);
        boolean hasLegacyMainImage = !hasPersistedMainImage
                && room.getMainImageUrl() != null
                && !room.getMainImageUrl().isBlank();
        int currentImageCount = room.getImages().size() + (hasLegacyMainImage ? 1 : 0);

        if (currentImageCount + requests.size() > 10) {
            throw new IllegalArgumentException("A room can have up to 10 images.");
        }

        long existingMainCount = room.getImages().stream()
                .filter(image -> image.getImageType() == RoomImageType.MAIN)
                .count();
        if (hasLegacyMainImage) {
            existingMainCount++;
        }
        long requestedMainCount = requests.stream()
                .filter(request -> request.imageType() == RoomImageType.MAIN)
                .count();
        if (existingMainCount + requestedMainCount > 1) {
            throw new IllegalArgumentException("A room can have only one main image.");
        }

        for (RoomImageCreateRequest request : requests) {
            RoomImage image = RoomImage.create(
                    room,
                    request.imageUrl(),
                    request.imageType(),
                    request.sortOrder()
            );
            room.addImage(image);
            if (request.imageType() == RoomImageType.MAIN) {
                room.updateMainImageUrl(request.imageUrl());
            }
        }

        roomRepository.saveAndFlush(room);

        return toImages(room);
    }

    private Room findRoomDetail(Long roomId) {
        return roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found: " + roomId
                ));
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
                toImages(room),
                toEquipmentGroups(room.getEquipmentMappings())
        );
    }

    private List<RoomImageResponse> toImages(Room room) {
        List<RoomImageResponse> images = new ArrayList<>();
        boolean hasPersistedMainImage = room.getImages().stream()
                .anyMatch(image -> image.getImageType() == RoomImageType.MAIN);

        if (!hasPersistedMainImage
                && room.getMainImageUrl() != null
                && !room.getMainImageUrl().isBlank()) {
            images.add(new RoomImageResponse(
                    null,
                    room.getMainImageUrl(),
                    RoomImageType.MAIN,
                    0
            ));
        }

        images.addAll(room.getImages().stream()
                .map(this::toImageResponse)
                .toList());
        return List.copyOf(images);
    }

    private RoomImageResponse toImageResponse(RoomImage image) {
        return new RoomImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getImageType(),
                image.getSortOrder()
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

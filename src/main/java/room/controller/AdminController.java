package room.controller;

import room.dto.request.RoomCreateRequest;
import room.dto.request.RoomEquipmentsUpdateRequest;
import room.dto.request.RoomImageCreateRequest;
import room.dto.request.RoomUpdateRequest;
import room.dto.response.RoomDetailResponse;
import room.dto.response.RoomImageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PostMapping("/accommodations/{accommodationId}/rooms")
    public ResponseEntity<RoomDetailResponse> createRoom(
            @PathVariable Long accommodationId,
            @RequestBody RoomCreateRequest request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/rooms/{roomId}/equipments")
    public ResponseEntity<Void> updateRoomEquipments(
            @PathVariable Long roomId,
            @RequestBody RoomEquipmentsUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/rooms/{roomId}/images")
    public ResponseEntity<RoomImageResponse> createRoomImage(
            @PathVariable Long roomId,
            @RequestBody RoomImageCreateRequest request
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/rooms/{roomId}/images/{imageId}")
    public ResponseEntity<Void> deleteRoomImage(
            @PathVariable Long roomId,
            @PathVariable Long imageId
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

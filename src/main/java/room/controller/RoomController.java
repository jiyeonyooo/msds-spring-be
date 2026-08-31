package Room.controller;

import Room.dto.response.RoomAvailabilityResponse;
import Room.dto.response.RoomDetailResponse;
import Room.dto.response.RoomSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RoomController {

    @GetMapping("/accommodations/{accommodationId}/rooms")
    public ResponseEntity<List<RoomSummaryResponse>> getRooms(
            @PathVariable Long accommodationId
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoom(@PathVariable Long roomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/rooms/{roomId}/availability")
    public ResponseEntity<RoomAvailabilityResponse> getAvailability(
            @PathVariable Long roomId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam Integer guests
    ) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

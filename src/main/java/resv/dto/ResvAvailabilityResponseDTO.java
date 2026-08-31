package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import resv.repository.ResvRoomUnitRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ResvAvailabilityResponseDTO(
        @JsonProperty("check_in_date") LocalDate checkInDate,
        @JsonProperty("check_out_date") LocalDate checkOutDate,
        @JsonProperty("guest_count") Integer guestCount,
        Integer nights,
        List<RoomAvailabilityDTO> rooms
) {
    public static ResvAvailabilityResponseDTO of(ResvAvailabilityRequestDTO request,
                                                 List<ResvRoomUnitRepository.RoomAvailabilityProjection> rooms) {
        int nights = Math.toIntExact(ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate()));
        return new ResvAvailabilityResponseDTO(request.getCheckInDate(), request.getCheckOutDate(), request.getGuestCount(), nights,
                rooms.stream().map(room -> new RoomAvailabilityDTO(room.getRoomId(), room.getRoomName(), room.getMaxGuests(),
                        room.getRemainingCount(), room.getBasePrice(), Math.multiplyExact(room.getBasePrice(), nights),
                        room.getRemainingCount() > 0)).toList());
    }

    public record RoomAvailabilityDTO(
            @JsonProperty("room_id") Long roomId,
            @JsonProperty("room_name") String roomName,
            @JsonProperty("max_guests") Integer maxGuests,
            @JsonProperty("remaining_count") long remainingCount,
            @JsonProperty("base_price") Integer basePrice,
            @JsonProperty("total_price") Integer totalPrice,
            Boolean available
    ) { }
}

package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import resv.entity.Resv;
import room.entity.Room;
import room.entity.RoomUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record CreateResvResponseDTO(
        @JsonProperty("resv_id") Long resvId,
        @JsonProperty("resv_number") String resvNumber,
        @JsonProperty("member_id") Long memberId,
        @JsonProperty("room_units_id") Long roomUnitsId,
        @JsonProperty("room_id") Long roomId,
        @JsonProperty("room_number") String roomNumber,
        @JsonProperty("room_name") String roomName,
        @JsonProperty("check_in_date") java.time.LocalDate checkInDate,
        @JsonProperty("check_out_date") java.time.LocalDate checkOutDate,
        @JsonProperty("guest_count") Integer guestCount,
        Integer nights,
        @JsonProperty("price_per_night") Integer pricePerNight,
        @JsonProperty("total_price") Integer totalPrice,
        @JsonProperty("resv_status") String resvStatus,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
    public static CreateResvResponseDTO from(Resv resv, Room room, RoomUnit roomUnit) {
        return new CreateResvResponseDTO(
                resv.getResvId(),
                resv.getResvNumber(),
                resv.getMemberId(),
                resv.getRoomUnitsId(),
                room.getId(),
                roomUnit.getRoomNumber(),
                room.getName(),
                resv.getCheckInDate(),
                resv.getCheckOutDate(),
                resv.getGuestCount(),
                Math.toIntExact(ChronoUnit.DAYS.between(resv.getCheckInDate(), resv.getCheckOutDate())),
                resv.getPricePerNight(),
                resv.getTotalPrice(),
                resv.getResvStatus().name(),
                resv.getCreatedAt());
    }
}

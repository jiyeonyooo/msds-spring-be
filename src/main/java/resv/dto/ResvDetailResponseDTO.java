package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResvDetailResponseDTO(
        @JsonProperty("resv_id") Long resvId,
        @JsonProperty("resv_number") String resvNumber,
        @JsonProperty("member_id") Long memberId,
        @JsonProperty("member_name") String memberName,
        @JsonProperty("phone_number") String phoneNumber,
        @JsonProperty("room_units_id") Long roomUnitsId,
        @JsonProperty("room_id") Long roomId,
        @JsonProperty("room_number") String roomNumber,
        @JsonProperty("room_name") String roomName,
        @JsonProperty("check_in_date") LocalDate checkInDate,
        @JsonProperty("check_out_date") LocalDate checkOutDate,
        @JsonProperty("guest_count") Integer guestCount,
        Integer nights,
        @JsonProperty("price_per_night") Integer pricePerNight,
        @JsonProperty("total_price") Integer totalPrice,
        @JsonProperty("resv_status") String resvStatus,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("cancelled_at") LocalDateTime cancelledAt
) { }

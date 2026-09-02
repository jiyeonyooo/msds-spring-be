package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// GET /api/admin/users/{userId}/activity 응답. 회원 상세 화면에서 예약·문의 이력을 함께 보여줄 때 사용한다.
public record AdminUserActivityResponse(
        @JsonProperty("user_id") Long userId,
        List<ReservationItem> reservations,
        List<InquiryItem> inquiries
) {
    public record ReservationItem(
            @JsonProperty("resv_id") Long resvId,
            @JsonProperty("resv_number") String resvNumber,
            @JsonProperty("room_name") String roomName,
            @JsonProperty("room_number") String roomNumber,
            @JsonProperty("check_in_date") LocalDate checkInDate,
            @JsonProperty("check_out_date") LocalDate checkOutDate,
            @JsonProperty("guest_count") Integer guestCount,
            @JsonProperty("total_price") Integer totalPrice,
            @JsonProperty("resv_status") String resvStatus,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("created_at") LocalDateTime createdAt
    ) { }

    public record InquiryItem(
            @JsonProperty("inquiry_id") Long inquiryId,
            String title,
            String status,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("answered_at") LocalDateTime answeredAt,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("created_at") LocalDateTime createdAt
    ) { }
}

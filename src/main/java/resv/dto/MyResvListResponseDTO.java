package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MyResvListResponseDTO(
        @JsonProperty("resv_list") List<MyResvListItemDTO> resvList,
        @JsonProperty("page_num") int pageNum,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages
) {
    public static MyResvListResponseDTO of(Page<MyResvListItemDTO> page) {
        return new MyResvListResponseDTO(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public record MyResvListItemDTO(
            @JsonProperty("resv_id") Long resvId,
            @JsonProperty("resv_number") String resvNumber,
            @JsonProperty("room_units_id") Long roomUnitsId,
            @JsonProperty("room_id") Long roomId,
            @JsonProperty("room_number") String roomNumber,
            @JsonProperty("room_name") String roomName,
            @JsonProperty("check_in_date") LocalDate checkInDate,
            @JsonProperty("check_out_date") LocalDate checkOutDate,
            @JsonProperty("guest_count") Integer guestCount,
            @JsonProperty("total_price") Integer totalPrice,
            @JsonProperty("resv_status") String resvStatus,
            @JsonProperty("created_at") LocalDateTime createdAt
    ) { }
}

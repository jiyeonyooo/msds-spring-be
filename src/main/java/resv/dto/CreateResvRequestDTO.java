package resv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import resv.validation.ValidDateRange;

import java.time.LocalDate;

//예약 내역 생성 Request DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange
public class CreateResvRequestDTO {
    @NotNull(message = "객실은 필수입니다.")
    @JsonProperty("room_id")
    private Long roomId;

    @NotNull(message = "체크인 날짜는 필수입니다.")
    @FutureOrPresent(message = "체크인 날짜는 오늘 이후여야 합니다.")
    @JsonProperty("check_in_date")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    @JsonProperty("check_out_date")
    private LocalDate checkOutDate;

    @NotNull(message = "숙박 인원은 필수입니다.")
    @Min(value = 1, message = "숙박 인원은 1명 이상이어야 합니다.")
    @JsonProperty("guest_count")
    private Integer guestCount;
}

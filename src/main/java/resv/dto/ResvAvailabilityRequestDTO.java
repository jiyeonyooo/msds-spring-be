package resv.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.BindParam;
import resv.validation.ValidDateRange;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ValidDateRange
public class ResvAvailabilityRequestDTO {

    @NotNull(message = "체크인 날짜는 필수입니다.")
    @FutureOrPresent(message = "체크인 날짜는 오늘 이후여야 합니다.")
    @BindParam("check_in_date")
    private LocalDate checkInDate;

    public void setCheck_in_date(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    @BindParam("check_out_date")
    private LocalDate checkOutDate;

    public void setCheck_out_date(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    @NotNull(message = "숙박 인원은 필수입니다.")
    @Min(value = 1, message = "숙박 인원은 1명 이상이어야 합니다.")
    @BindParam("guest_count")
    private Integer guestCount;

    public void setGuest_count(Integer guestCount) {
        this.guestCount = guestCount;
    }
}

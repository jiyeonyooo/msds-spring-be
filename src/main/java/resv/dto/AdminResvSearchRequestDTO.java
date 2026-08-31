package resv.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.BindParam;
import resv.enums.ResvStatus;
import resv.validation.ValidDateRange;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ValidDateRange(startField = "searchFromDate", endField = "searchToDate", message = "검색 종료일은 검색 시작일보다 이전일 수 없습니다.")
public class AdminResvSearchRequestDTO {

    @BindParam("resv_status")
    private ResvStatus resvStatus;

    @BindParam("search_from_date")
    private LocalDate searchFromDate;

    @BindParam("search_to_date")
    private LocalDate searchToDate;

    private String keyword;

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    @BindParam("page_num")
    private Integer pageNum = 0;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @BindParam("page_size")
    private Integer pageSize = 10;
}

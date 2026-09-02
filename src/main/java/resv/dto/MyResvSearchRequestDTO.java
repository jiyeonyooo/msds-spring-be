package resv.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.BindParam;
import resv.enums.ResvStatus;

@Getter
@Setter
@NoArgsConstructor
public class MyResvSearchRequestDTO {

    @BindParam("resv_status")
    private ResvStatus resvStatus;

    public void setResv_status(ResvStatus resvStatus) {
        this.resvStatus = resvStatus;
    }

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    @BindParam("page_num")
    private Integer pageNum = 0;

    public void setPage_num(Integer pageNum) {
        this.pageNum = pageNum;
    }

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @BindParam("page_size")
    private Integer pageSize = 10;

    public void setPage_size(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

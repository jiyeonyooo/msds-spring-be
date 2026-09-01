package room.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import room.entity.enums.FacilityCategory;

public record FacilityUpdateRequest(
        @Size(min = 1, max = 100, message = "편의시설명은 1자 이상 100자 이하여야 합니다.")
        @Pattern(regexp = ".*\\S.*", message = "편의시설명은 공백일 수 없습니다.")
        String name,

        FacilityCategory category,

        @Size(max = 255, message = "편의시설 설명은 255자 이하여야 합니다.")
        String description,

        @Size(max = 512, message = "이미지 URL은 512자 이하여야 합니다.")
        String imageUrl,

        Boolean active
) {
    @AssertTrue(message = "수정할 편의시설 정보를 하나 이상 입력해야 합니다.")
    public boolean isAnyFieldPresent() {
        return name != null
                || category != null
                || description != null
                || imageUrl != null
                || active != null;
    }
}

package room.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import room.entity.enums.FacilityCategory;

public record FacilityCreateRequest(
        @NotBlank(message = "편의시설명은 필수입니다.")
        @Size(max = 100, message = "편의시설명은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "편의시설 카테고리는 필수입니다.")
        FacilityCategory category,

        @Size(max = 255, message = "편의시설 설명은 255자 이하여야 합니다.")
        String description,

        @Size(max = 512, message = "이미지 URL은 512자 이하여야 합니다.")
        String imageUrl,

        Boolean active
) {
}

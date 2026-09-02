package room.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import room.entity.enums.BedType;

import java.math.BigDecimal;

public record RoomCreateRequest(
        @NotBlank(message = "객실명은 필수입니다.")
        @Size(max = 100, message = "객실명은 100자 이하여야 합니다.")
        String name,

        @NotBlank(message = "객실 상세 설명은 필수입니다.")
        String description,

        @NotNull(message = "객실 유형은 필수입니다.")
        RoomType roomType,

        @NotNull(message = "판매 상태는 필수입니다.")
        RoomStatus status,

        @NotNull(message = "최소 숙박 인원은 필수입니다.")
        @Min(value = 1, message = "최소 숙박 인원은 1명 이상이어야 합니다.")
        Integer minGuest,

        @NotNull(message = "최대 숙박 인원은 필수입니다.")
        @Min(value = 1, message = "최대 숙박 인원은 1명 이상이어야 합니다.")
        Integer maxGuest,

        @NotNull(message = "객실 면적은 필수입니다.")
        @DecimalMin(value = "0.01", message = "객실 면적은 0보다 커야 합니다.")
        BigDecimal area,

        @NotNull(message = "1박 기본 가격은 필수입니다.")
        @Min(value = 0, message = "1박 기본 가격은 0원 이상이어야 합니다.")
        Integer basePrice,

        @Size(max = 512, message = "대표 이미지 URL은 512자 이하여야 합니다.")
        String mainImageUrl,

        BedType bedType,

        @Min(value = 1, message = "침대 수는 1개 이상이어야 합니다.")
        Integer bedCount
) {

    @AssertTrue(message = "최대 숙박 인원은 최소 숙박 인원 이상이어야 합니다.")
    public boolean isGuestRangeValid() {
        return minGuest == null || maxGuest == null || minGuest <= maxGuest;
    }
}

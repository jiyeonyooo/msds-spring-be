package room.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import room.entity.enums.BedType;

import java.math.BigDecimal;

public record RoomUpdateRequest(
        @Size(min = 1, max = 100, message = "객실명은 1자 이상 100자 이하여야 합니다.")
        String name,

        @Size(min = 1, message = "객실 상세 설명은 비어 있을 수 없습니다.")
        String description,

        RoomType roomType,
        RoomStatus status,

        @Min(value = 1, message = "최소 숙박 인원은 1명 이상이어야 합니다.")
        Integer minGuest,

        @Min(value = 1, message = "최대 숙박 인원은 1명 이상이어야 합니다.")
        Integer maxGuest,

        @DecimalMin(value = "0.01", message = "객실 면적은 0보다 커야 합니다.")
        BigDecimal area,

        @Min(value = 0, message = "1박 기본 가격은 0원 이상이어야 합니다.")
        Integer basePrice,

        @Size(max = 512, message = "대표 이미지 URL은 512자 이하여야 합니다.")
        String mainImageUrl,

        BedType bedType,

        @Min(value = 1, message = "침대 수는 1개 이상이어야 합니다.")
        Integer bedCount
) {

    @AssertTrue(message = "수정할 객실 정보를 하나 이상 입력해야 합니다.")
    public boolean isAnyFieldPresent() {
        return name != null
                || description != null
                || roomType != null
                || status != null
                || minGuest != null
                || maxGuest != null
                || area != null
                || basePrice != null
                || mainImageUrl != null
                || bedType != null
                || bedCount != null;
    }
}

package room.dto.response;

import room.entity.enums.FacilityCategory;

import java.time.LocalDateTime;

public record FacilityDetailResponse(
        Long facilityId,
        String name,
        FacilityCategory category,
        String description,
        String imageUrl,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

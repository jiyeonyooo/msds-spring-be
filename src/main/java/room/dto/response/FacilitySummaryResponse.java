package room.dto.response;

import room.entity.enums.FacilityCategory;

public record FacilitySummaryResponse(
        Long facilityId,
        String name,
        FacilityCategory category,
        String description,
        String imageUrl
) {}

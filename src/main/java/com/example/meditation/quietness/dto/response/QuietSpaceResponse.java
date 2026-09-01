package com.example.meditation.quietness.dto.response;

import com.example.meditation.quietness.entity.QuietSpaceType;

public record QuietSpaceResponse(
        Long spaceId,
        Long guesthouseId,
        String name,
        QuietSpaceType type,
        boolean active
) {
}

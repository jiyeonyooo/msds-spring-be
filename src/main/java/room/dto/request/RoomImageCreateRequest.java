package room.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import room.entity.enums.RoomImageType;

public record RoomImageCreateRequest(
        @NotBlank
        @Size(max = 512)
        @Pattern(regexp = "^/uploads/rooms/[A-Za-z0-9._-]+$", message = "Invalid room image URL.")
        String imageUrl,

        @NotNull
        RoomImageType imageType,

        @NotNull
        @Min(0)
        Integer sortOrder
) {}

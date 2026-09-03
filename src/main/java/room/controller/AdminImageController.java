package room.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import global.file.ImageCategory;
import global.file.LocalImageStorageService;
import global.file.StoredImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/images")
@RequiredArgsConstructor
@Tag(name = "관리자 - 이미지")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminImageController {

    private final LocalImageStorageService imageStorageService;

    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "객실 이미지 업로드", description = "JPG, PNG, WEBP 이미지를 최대 10MB까지 업로드합니다.")
    public ResponseEntity<ApiResponse<StoredImage>> uploadRoomImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Room image uploaded successfully.",
                imageStorageService.store(image, ImageCategory.ROOM)
        );
    }

    @PostMapping(value = "/facilities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "편의시설 이미지 업로드", description = "JPG, PNG, WEBP 이미지를 최대 10MB까지 업로드합니다.")
    public ResponseEntity<ApiResponse<StoredImage>> uploadFacilityImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Facility image uploaded successfully.",
                imageStorageService.store(image, ImageCategory.FACILITY)
        );
    }
}

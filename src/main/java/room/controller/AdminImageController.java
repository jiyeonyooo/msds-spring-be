package room.controller;

import global.dto.response.ApiResponse;
import global.file.ImageCategory;
import global.file.LocalImageStorageService;
import global.file.StoredImage;
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
public class AdminImageController {

    private final LocalImageStorageService imageStorageService;

    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StoredImage>> uploadRoomImage(
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Room image uploaded successfully.",
                imageStorageService.store(image, ImageCategory.ROOM)
        );
    }

    @PostMapping(value = "/facilities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StoredImage>> uploadFacilityImage(
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Facility image uploaded successfully.",
                imageStorageService.store(image, ImageCategory.FACILITY)
        );
    }
}

package room.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import room.dto.request.RoomCreateRequest;
import room.dto.request.RoomEquipmentsUpdateRequest;
import room.dto.request.RoomImageCreateRequest;
import room.dto.request.RoomUpdateRequest;
import room.dto.request.FacilityCreateRequest;
import room.dto.request.FacilityUpdateRequest;
import room.dto.response.FacilityDetailResponse;
import room.dto.response.RoomDetailResponse;
import room.dto.response.RoomEquipmentOptionResponse;
import room.dto.response.RoomImageResponse;
import room.service.FacilityService;
import room.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 - 객실·시설")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

    private final RoomService roomService;
    private final FacilityService facilityService;

    @GetMapping("/rooms")
    @Operation(summary = "객실 목록 조회")
    public ResponseEntity<ApiResponse<List<RoomDetailResponse>>> getRooms() {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 객실 목록 조회에 성공했습니다.",
                roomService.getRoomsForAdmin()
        );
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "객실 상세 조회")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoom(@PathVariable Long roomId) {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 객실 상세 조회에 성공했습니다.",
                roomService.getRoom(roomId)
        );
    }

    @GetMapping("/facilities")
    @Operation(summary = "편의시설 목록 조회")
    public ResponseEntity<ApiResponse<List<FacilityDetailResponse>>> getFacilities() {
        return ApiResponse.success(
                HttpStatus.OK,
                "전체 편의시설 목록 조회에 성공했습니다.",
                facilityService.getAllFacilities()
        );
    }

    @GetMapping("/facilities/{facilityId}")
    @Operation(summary = "편의시설 상세 조회")
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> getFacility(
            @PathVariable Long facilityId
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "편의시설 상세 조회에 성공했습니다.",
                facilityService.getFacility(facilityId)
        );
    }

    @PostMapping("/rooms")
    @Operation(summary = "객실 등록")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> createRoom(
            @Valid @RequestBody RoomCreateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "객실 등록에 성공했습니다.",
                roomService.createRoom(request)
        );
    }

    @PatchMapping("/rooms/{roomId}")
    @Operation(summary = "객실 정보 수정")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 정보 수정에 성공했습니다.",
                roomService.updateRoom(roomId, request)
        );
    }

    @GetMapping("/room-equipments")
    @Operation(summary = "객실 비품 선택지 조회")
    public ResponseEntity<ApiResponse<List<RoomEquipmentOptionResponse>>> getRoomEquipments() {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 비품 목록 조회에 성공했습니다.",
                roomService.getEquipmentOptions()
        );
    }

    @PatchMapping("/rooms/{roomId}/equipments")
    @Operation(summary = "객실 비품 수정", description = "객실에 연결할 비품 목록을 교체합니다.")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> updateRoomEquipments(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomEquipmentsUpdateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 비품 수정에 성공했습니다.",
                roomService.updateRoomEquipments(roomId, request)
        );
    }

    @PostMapping("/rooms/{roomId}/images")
    @Operation(summary = "객실 이미지 정보 등록", description = "이미지 업로드 API가 반환한 URL과 정렬 순서를 객실에 연결합니다.")
    public ResponseEntity<ApiResponse<List<RoomImageResponse>>> addRoomImages(
            @PathVariable Long roomId,
            @Valid @RequestBody List<@Valid RoomImageCreateRequest> requests
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Room images registered successfully.",
                roomService.addRoomImages(roomId, requests)
        );
    }

    @PostMapping("/facilities")
    @Operation(summary = "편의시설 등록")
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> createFacility(
            @Valid @RequestBody FacilityCreateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "편의시설 등록에 성공했습니다.",
                facilityService.createFacility(request)
        );
    }

    @PatchMapping("/facilities/{facilityId}")
    @Operation(summary = "편의시설 정보 수정")
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> updateFacility(
            @PathVariable Long facilityId,
            @Valid @RequestBody FacilityUpdateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "편의시설 정보 수정에 성공했습니다.",
                facilityService.updateFacility(facilityId, request)
        );
    }
}

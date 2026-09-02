package room.controller;

import global.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import room.dto.request.RoomCreateRequest;
import room.dto.request.RoomUpdateRequest;
import room.dto.request.FacilityCreateRequest;
import room.dto.request.FacilityUpdateRequest;
import room.dto.response.FacilityDetailResponse;
import room.dto.response.RoomDetailResponse;
import room.service.FacilityService;
import room.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RoomService roomService;
    private final FacilityService facilityService;

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomDetailResponse>>> getRooms() {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 객실 목록 조회에 성공했습니다.",
                roomService.getRoomsForAdmin()
        );
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoom(@PathVariable Long roomId) {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 객실 상세 조회에 성공했습니다.",
                roomService.getRoom(roomId)
        );
    }

    @PostMapping("/rooms")
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

    @PostMapping("/facilities")
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> createFacility(
            @Valid @RequestBody FacilityCreateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "편의시설 등록에 성공했습니다.",
                facilityService.createFacility(request)
        );
    }

    @GetMapping("/facilities")
    public ResponseEntity<ApiResponse<List<FacilityDetailResponse>>> getFacilities() {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 편의시설 목록 조회에 성공했습니다.",
                facilityService.getFacilitiesForAdmin()
        );
    }

    @GetMapping("/facilities/{facilityId}")
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> getFacility(
            @PathVariable Long facilityId
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "관리자 편의시설 상세 조회에 성공했습니다.",
                facilityService.getFacility(facilityId)
        );
    }

    @PatchMapping("/facilities/{facilityId}")
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

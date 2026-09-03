package room.controller;

import global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import room.dto.response.RoomDetailResponse;
import room.dto.response.RoomSummaryResponse;
import room.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "객실")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "객실 목록 조회")
    public ResponseEntity<ApiResponse<List<RoomSummaryResponse>>> getRooms() {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 목록 조회에 성공했습니다.",
                roomService.getRooms()
        );
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "객실 상세 조회", description = "객실 기본 정보, 이미지, 비품 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoom(
            @PathVariable Long roomId
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 상세 조회에 성공했습니다.",
                roomService.getRoom(roomId)
        );
    }
}

package room.controller;

import global.dto.response.ApiResponse;
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
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomSummaryResponse>>> getRooms() {
        return ApiResponse.success(
                HttpStatus.OK,
                "객실 목록 조회에 성공했습니다.",
                roomService.getRooms()
        );
    }

    @GetMapping("/{roomId}")
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

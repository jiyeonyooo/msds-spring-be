package Room.controller;

import Room.dto.response.RoomDetailResponse;
import Room.dto.response.RoomSummaryResponse;
import Room.service.RoomService;
import global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomSummaryResponse>>> getRooms() {
        return ResponseEntity.ok(ApiResponse.success(
                "객실 목록 조회에 성공했습니다.",
                roomService.getRooms()
        ));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success(
                "객실 상세 조회에 성공했습니다.",
                roomService.getRoom(roomId)
        ));
    }
}

package resv.controller;

import global.dto.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import resv.dto.AdminResvSearchRequestDTO;
import resv.dto.ResvCancelResponseDTO;
import resv.dto.ResvDetailResponseDTO;
import resv.dto.ResvListResponseDTO;
import resv.service.ResvService;

@RestController
@RequestMapping("/api/admin/resv")
@Validated
@RequiredArgsConstructor
public class AdminResvController {
    private final ResvService resvService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResvListResponseDTO>> getReservations(@Valid @ModelAttribute AdminResvSearchRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAdminReservations(request)));
    }

    @GetMapping("/{resvId}")
    public ResponseEntity<ApiResponse<ResvDetailResponseDTO>> getReservation(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAdminReservation(resvId)));
    }

    @PatchMapping("/{resvId}/status")
    public ResponseEntity<ApiResponse<ResvCancelResponseDTO>> cancelReservation(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.", resvService.cancelAdminReservation(resvId)));
    }
}

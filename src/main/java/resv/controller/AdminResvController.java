package resv.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "관리자 - 예약")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminResvController {
    private final ResvService resvService;

    @GetMapping
    @Operation(summary = "전체 예약 목록 조회", description = "검색 조건에 맞는 예약을 페이지 단위로 조회합니다.")
    public ResponseEntity<ApiResponse<ResvListResponseDTO>> getReservations(@Valid @ModelAttribute AdminResvSearchRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAdminReservations(request)));
    }

    @GetMapping("/{resvId}")
    @Operation(summary = "예약 상세 조회")
    public ResponseEntity<ApiResponse<ResvDetailResponseDTO>> getReservation(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAdminReservation(resvId)));
    }

    @PatchMapping("/{resvId}/status")
    @Operation(summary = "예약 취소")
    public ResponseEntity<ApiResponse<ResvCancelResponseDTO>> cancelReservation(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.", resvService.cancelAdminReservation(resvId)));
    }

    @PatchMapping("/{resvId}/restore")
    @Operation(summary = "취소 예약 복구")
    public ResponseEntity<ApiResponse<ResvCancelResponseDTO>> restoreReservation(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("예약이 복구되었습니다.", resvService.restoreAdminReservation(resvId)));
    }
}

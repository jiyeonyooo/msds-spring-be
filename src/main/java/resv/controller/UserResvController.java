package resv.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import resv.dto.*;
import resv.service.ResvService;

@RestController
@RequestMapping("/api/resv")
@Validated
@RequiredArgsConstructor
@Tag(name = "예약")
public class UserResvController {
    private final ResvService resvService;

    @GetMapping
    @Operation(summary = "예약 가능 객실 조회", description = "체크인·체크아웃 날짜와 인원에 맞는 객실 재고를 조회합니다.")
    public ResponseEntity<ApiResponse<ResvAvailabilityResponseDTO>> getAvailability(@Valid @ModelAttribute ResvAvailabilityRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAvailability(request)));
    }

    @PostMapping
    @Operation(summary = "객실 예약 생성")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<CreateResvResponseDTO>> createResv(
                                                                           @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
                                                                           @Valid @RequestBody CreateResvRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("예약이 성공적으로 생성되었습니다.",
                resvService.create(principal.getUsername(), request)));
    }

    @GetMapping("/me")
    @Operation(summary = "내 예약 목록 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<MyResvListResponseDTO>> getMyReservations(
                                                                                   @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
                                                                                   @Valid @ModelAttribute MyResvSearchRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.",
                resvService.getMyReservations(principal.getUsername(), request)));
    }

    @GetMapping("/{resvId}")
    @Operation(summary = "내 예약 상세 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<MyResvDetailResponseDTO>> getMyReservation(
                                                                                    @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
                                                                                    @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.",
                resvService.getMyReservation(principal.getUsername(), resvId)));
    }

    @PatchMapping("/{resvId}/cancel")
    @Operation(summary = "내 예약 취소")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<ResvCancelResponseDTO>> cancelMyReservation(
                                                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
                                                                                     @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.",
                resvService.cancelMyReservation(principal.getUsername(), resvId)));
    }
}

package resv.controller;

import global.dto.response.ApiResponse;
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
public class UserResvController {
    private final ResvService resvService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResvAvailabilityResponseDTO>> getAvailability(@Valid @ModelAttribute ResvAvailabilityRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", resvService.getAvailability(request)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateResvResponseDTO>> createResv(@AuthenticationPrincipal UserDetails principal,
                                                                           @Valid @RequestBody CreateResvRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("예약이 성공적으로 생성되었습니다.",
                resvService.create(principal.getUsername(), request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyResvListResponseDTO>> getMyReservations(@AuthenticationPrincipal UserDetails principal,
                                                                                   @Valid @ModelAttribute MyResvSearchRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.",
                resvService.getMyReservations(principal.getUsername(), request)));
    }

    @GetMapping("/{resvId}")
    public ResponseEntity<ApiResponse<MyResvDetailResponseDTO>> getMyReservation(@AuthenticationPrincipal UserDetails principal,
                                                                                    @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.",
                resvService.getMyReservation(principal.getUsername(), resvId)));
    }

    @PatchMapping("/{resvId}/cancel")
    public ResponseEntity<ApiResponse<ResvCancelResponseDTO>> cancelMyReservation(@AuthenticationPrincipal UserDetails principal,
                                                                                     @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.",
                resvService.cancelMyReservation(principal.getUsername(), resvId)));
    }
}

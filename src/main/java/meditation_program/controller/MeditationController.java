package meditation_program.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meditation_program.dto.*;
import meditation_program.service.ProgramService;
import meditation_program.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meditation")
@RequiredArgsConstructor
@Tag(name = "명상 프로그램", description = "명상 프로그램 조회, 신청, 취소, 후기와 관리자 기능을 제공합니다.")
public class MeditationController {

    private final ProgramService programService;
    private final ReviewService reviewService;

    @GetMapping("/program")
    @Operation(summary = "명상 프로그램 목록 조회")
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> programList() {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getPrograms()));
    }

    @PostMapping("/program")
    @Operation(summary = "명상 프로그램 신청")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Long>> reserveProgram(
                                                            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestBody @Valid ReservationRequest request) {
        Long reservationId = programService.reserve(userDetails.getUsername(), request);
        return ApiResponse.success(HttpStatus.CREATED, "예약되었습니다.", reservationId);
    }

    @DeleteMapping("/program/reservation/{reservationId}")
    @Operation(summary = "명상 프로그램 신청 취소")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
                                                               @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                                                               @PathVariable Long reservationId) {
        programService.cancelReservation(userDetails.getUsername(), reservationId);
        return ApiResponse.success(HttpStatus.OK, "취소되었습니다.", null);
    }

    @GetMapping("/program/reservations/me")
    @Operation(summary = "내 명상 프로그램 신청 목록 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> myReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getMyReservations(userDetails.getUsername())));
    }

    @GetMapping("/review")
    @Operation(summary = "명상 프로그램 후기 목록 조회")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> reviewList() {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", reviewService.getReviews()));
    }

    @GetMapping("/review/me")
    @Operation(summary = "내 후기 목록 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> myReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", reviewService.getMyReviews(userDetails.getUsername())));
    }

    @PostMapping("/review")
    @Operation(summary = "명상 프로그램 후기 등록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Long>> addReview(
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody @Valid ReviewCreateRequest request) {
        Long reviewId = reviewService.addReview(userDetails.getUsername(), request);
        return ApiResponse.success(HttpStatus.CREATED, "후기가 등록되었습니다.", reviewId);
    }

    @DeleteMapping("/review/{reviewId}")
    @Operation(summary = "내 후기 삭제")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Void>> deleteReview(
                                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long reviewId) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }

    // --- 관리자 ---
    @PostMapping("/admin/program")
    @Operation(summary = "명상 프로그램 등록", tags = "관리자 - 명상 프로그램")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Long>> createProgram(@RequestBody @Valid ProgramCreateRequest request) {
        Long id = programService.createProgram(request);
        return ApiResponse.success(HttpStatus.CREATED, "프로그램이 생성되었습니다.", id);
    }

    @GetMapping("/admin/program/{programId}/applications")
    @Operation(summary = "프로그램 신청자 목록 조회", tags = "관리자 - 명상 프로그램")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<List<ProgramApplicationResponse>>> programApplications(
            @PathVariable Long programId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "신청자 목록을 조회했습니다.",
                programService.getApplications(programId)
        ));
    }

    @PatchMapping("/admin/program/{programId}")
    @Operation(summary = "명상 프로그램 수정", tags = "관리자 - 명상 프로그램")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Void>> updateProgram(
            @PathVariable Long programId,
            @RequestBody @Valid ProgramUpdateRequest request
    ) {
        programService.updateProgram(programId, request);
        return ApiResponse.success(HttpStatus.OK, "프로그램이 수정되었습니다.", null);
    }

    @DeleteMapping("/admin/program/{programId}")
    @Operation(summary = "명상 프로그램 삭제", tags = "관리자 - 명상 프로그램")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }

    @GetMapping("/program/reservations")
    @Operation(summary = "내 프로그램 예약 정보 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<List<ProgramReservationResponse>>> myProgramReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getMyProgramReservations(userDetails.getUsername())));
    }

    @GetMapping("/program/detail/{programId}")
    @Operation(summary = "명상 프로그램 상세 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<ApiResponse<ProgramResponse>> programDetail(@PathVariable Long programId) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getProgram(programId)));
    }
}

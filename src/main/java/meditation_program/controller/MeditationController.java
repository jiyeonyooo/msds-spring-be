package meditation_program.controller;

import global.dto.response.ApiResponse;
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
public class MeditationController {

    private final ProgramService programService;
    private final ReviewService reviewService;

    @GetMapping("/program")
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> programList() {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getPrograms()));
    }

    @PostMapping("/program")
    public ResponseEntity<ApiResponse<Long>> reserveProgram(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestBody @Valid ReservationRequest request) {
        Long reservationId = programService.reserve(userDetails.getUsername(), request);
        return ApiResponse.success(HttpStatus.CREATED, "예약되었습니다.", reservationId);
    }

    @DeleteMapping("/program/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@AuthenticationPrincipal UserDetails userDetails,
                                                               @PathVariable Long reservationId) {
        programService.cancelReservation(userDetails.getUsername(), reservationId);
        return ApiResponse.success(HttpStatus.OK, "취소되었습니다.", null);
    }

    @GetMapping("/program/reservations/me")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> myReservations(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", programService.getMyReservations(userDetails.getUsername())));
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> reviewList() {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", reviewService.getReviews()));
    }

    @GetMapping("/review/me")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> myReviews(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", reviewService.getMyReviews(userDetails.getUsername())));
    }

    @PostMapping("/review")
    public ResponseEntity<ApiResponse<Long>> addReview(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody @Valid ReviewCreateRequest request) {
        Long reviewId = reviewService.addReview(userDetails.getUsername(), request);
        return ApiResponse.success(HttpStatus.CREATED, "후기가 등록되었습니다.", reviewId);
    }

    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long reviewId) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }

    // --- 관리자 ---
    @PostMapping("/admin/program")
    public ResponseEntity<ApiResponse<Long>> createProgram(@RequestBody @Valid ProgramCreateRequest request) {
        Long id = programService.createProgram(request);
        return ApiResponse.success(HttpStatus.CREATED, "프로그램이 생성되었습니다.", id);
    }

    @DeleteMapping("/admin/program/{programId}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }
}
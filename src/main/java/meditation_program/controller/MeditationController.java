package meditation_program.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meditation_program.dto.*;
import meditation_program.service.ProgramService;
import meditation_program.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/meditation")
@RequiredArgsConstructor
public class MeditationController {

    private final ProgramService programService;
    private final ReviewService reviewService;

    @GetMapping("/program")
    public List<ProgramResponse> programList() {
        return programService.getPrograms();
    }

    @PostMapping("/program")
    // TODO: 인증 붙이면 @AuthenticationPrincipal로 원복 필요
    public ResponseEntity<Void> reserveProgram(//@AuthenticationPrincipal Long userId,
                                               @RequestParam Long userId,
                                               @RequestBody @Valid ReservationRequest request) {
        Long reservationId = programService.reserve(userId, request);
        return ResponseEntity.created(URI.create("/meditation/program/reservation/" + reservationId)).build();
    }

    @DeleteMapping("/program/reservation/{reservationId}")
    // TODO: 인증 붙이면 @AuthenticationPrincipal로 원복 필요
    public ResponseEntity<Void> cancelReservation(//@AuthenticationPrincipal Long userId,
                                                  @RequestParam Long userId,
                                                  @PathVariable Long reservationId) {
        programService.cancelReservation(userId, reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/review")
    public List<ReviewResponse> reviewList() {
        return reviewService.getReviews();
    }

    @PostMapping("/review")
    // TODO: 인증 붙이면 @AuthenticationPrincipal로 원복 필요
    public ResponseEntity<Void> addReview(//@AuthenticationPrincipal Long userId,
                                          @RequestParam Long userId,
                                          @RequestBody @Valid ReviewCreateRequest request) {
        Long reviewId = reviewService.addReview(userId, request);
        return ResponseEntity.created(URI.create("/meditation/review/" + reviewId)).build();
    }

    @DeleteMapping("/review/{reviewId}")
    // TODO: 인증 붙이면 @AuthenticationPrincipal로 원복 필요
    public ResponseEntity<Void> deleteReview(//@AuthenticationPrincipal Long userId,
                                             @RequestParam Long userId,
                                             @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }

    // --- 관리자 ---
    @PostMapping("/admin/program")
    public ResponseEntity<Void> createProgram(@RequestBody @Valid ProgramCreateRequest request) {
        Long id = programService.createProgram(request);
        return ResponseEntity.created(URI.create("/meditation/program/" + id)).build();
    }

    @DeleteMapping("/admin/program/{programId}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ResponseEntity.noContent().build();
    }
}

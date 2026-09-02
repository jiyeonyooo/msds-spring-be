package meditation_program.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meditation_program.dto.*;
import meditation_program.service.ProgramService;
import meditation_program.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<Void> reserveProgram(@AuthenticationPrincipal UserDetails principal,
                                               @RequestBody @Valid ReservationRequest request) {
        Long reservationId = programService.reserve(principal.getUsername(), request);
        return ResponseEntity.created(URI.create("/meditation/program/reservation/" + reservationId)).build();
    }

    @DeleteMapping("/program/reservation/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal UserDetails principal,
                                                  @PathVariable Long reservationId) {
        programService.cancelReservation(principal.getUsername(), reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/review")
    public List<ReviewResponse> reviewList() {
        return reviewService.getReviews();
    }

    @PostMapping("/review")
    public ResponseEntity<Void> addReview(@AuthenticationPrincipal UserDetails principal,
                                          @RequestBody @Valid ReviewCreateRequest request) {
        Long reviewId = reviewService.addReview(principal.getUsername(), request);
        return ResponseEntity.created(URI.create("/meditation/review/" + reviewId)).build();
    }

    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal UserDetails principal,
                                             @PathVariable Long reviewId) {
        reviewService.deleteReview(principal.getUsername(), reviewId);
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

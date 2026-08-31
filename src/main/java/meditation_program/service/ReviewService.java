package meditation_program.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import meditation_program.dto.ReviewCreateRequest;
import meditation_program.dto.ReviewResponse;
import meditation_program.entity.ProgramReservation;
import meditation_program.entity.Review;
import meditation_program.repository.ProgramReservationRepository;
import meditation_program.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProgramReservationRepository reservationRepository;

    public List<ReviewResponse> getReviews() {
        return reviewRepository.findAll().stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public Long addReview(Long memberId, @Valid ReviewCreateRequest request) {
        ProgramReservation reservation = reservationRepository.findById(request.programReservationId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));

        if (!reservation.getUser().getId().equals(memberId)) {
            throw new AccessDeniedException("본인이 참여한 예약에만 리뷰를 작성할 수 있습니다.");
        }
        if (reviewRepository.existsByProgramReservationId(reservation.getId())) {
            throw new IllegalStateException("이미 리뷰를 작성했습니다.");
        }

        Review review = Review.builder()
                .programReservation(reservation)
                .content(request.content())
                .build();
        return reviewRepository.save(review).getId();
    }

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다."));
        if (!review.getProgramReservation().getUser().getId().equals(memberId)) {
            throw new AccessDeniedException("본인 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }
}
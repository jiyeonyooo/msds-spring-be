package meditation_program.service;

import meditation_program.dto.ReviewCreateRequest;
import meditation_program.entity.Program;
import meditation_program.entity.ProgramReservation;
import meditation_program.entity.Review;
import meditation_program.repository.ProgramReservationRepository;
import meditation_program.repository.ReviewRepository;
import member.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ProgramReservationRepository reservationRepository = mock(ProgramReservationRepository.class);
    private final ReviewService service = new ReviewService(reviewRepository, reservationRepository);

    @Test
    void 로그인_이메일과_예약자_이메일이_같으면_후기를_작성한다() {
        ProgramReservation reservation = reservation("member@example.com");
        Review savedReview = mock(Review.class);
        when(reservationRepository.findById(3L)).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByProgramReservationId(reservation.getId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(savedReview.getId()).thenReturn(15L);

        Long reviewId = service.addReview("MEMBER@example.com", new ReviewCreateRequest(3L, "편안했습니다."));

        assertThat(reviewId).isEqualTo(15L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void 다른_회원의_후기는_삭제할_수_없다() {
        Review review = Review.builder()
                .programReservation(reservation("owner@example.com"))
                .content("좋았습니다.")
                .build();
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.deleteReview("other@example.com", 5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 리뷰만 삭제할 수 있습니다.");
    }

    private ProgramReservation reservation(String email) {
        Program program = Program.builder().name("Morning Silence Meditation").capacity(10).build();
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .name("테스트 회원")
                .phoneNumber("010-0000-0000")
                .role("USER")
                .build();
        return ProgramReservation.builder().program(program).user(user).quantity(1).build();
    }
}

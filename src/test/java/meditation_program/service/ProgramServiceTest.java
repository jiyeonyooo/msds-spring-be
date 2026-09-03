package meditation_program.service;

import meditation_program.dto.ReservationRequest;
import meditation_program.dto.ProgramUpdateRequest;
import meditation_program.entity.Program;
import meditation_program.entity.ProgramReservation;
import meditation_program.entity.ReservationStatus;
import meditation_program.repository.ProgramRepository;
import meditation_program.repository.ProgramReservationRepository;
import meditation_program.repository.ReviewRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramServiceTest {

    private final ProgramRepository programRepository = mock(ProgramRepository.class);
    private final ProgramReservationRepository reservationRepository = mock(ProgramReservationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ProgramService service = new ProgramService(
            programRepository,
            reservationRepository,
            userRepository,
            reviewRepository
    );

    @Test
    void 로그인_이메일로_프로그램을_신청한다() {
        Program program = Program.builder().name("Morning Silence Meditation").capacity(10).build();
        User user = member("member@example.com");
        ProgramReservation savedReservation = mock(ProgramReservation.class);
        when(programRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(program));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any(ProgramReservation.class))).thenReturn(savedReservation);
        when(savedReservation.getId()).thenReturn(27L);

        Long reservationId = service.reserve("member@example.com", new ReservationRequest(1L, 1));

        assertThat(reservationId).isEqualTo(27L);
        assertThat(program.getRemain()).isEqualTo(9);
        verify(userRepository).findByEmail("member@example.com");
    }

    @Test
    void 다른_회원의_프로그램_신청은_취소할_수_없다() {
        Program program = Program.builder().name("Ocean Breathing").capacity(8).build();
        ProgramReservation reservation = ProgramReservation.builder()
                .program(program)
                .user(member("owner@example.com"))
                .quantity(1)
                .build();
        when(reservationRepository.findById(9L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.cancelReservation("other@example.com", 9L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인 예약만 취소할 수 있습니다.");
    }

    @Test
    void 같은_회원은_같은_프로그램을_중복_신청할_수_없다() {
        Program program = mock(Program.class);
        User user = mock(User.class);
        when(programRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(program));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(program.getId()).thenReturn(1L);
        when(user.getId()).thenReturn(2L);
        when(reservationRepository.existsByProgramIdAndUserIdAndStatus(
                1L,
                2L,
                ReservationStatus.RESERVED
        )).thenReturn(true);

        assertThatThrownBy(() -> service.reserve(
                "member@example.com",
                new ReservationRequest(1L, 1)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 신청한 프로그램입니다.");

        verify(reservationRepository, never()).save(any(ProgramReservation.class));
    }

    @Test
    void 프로그램_수정시_기존_신청_인원을_제외한_잔여_인원을_계산한다() {
        Program program = Program.builder().name("Morning Silence").capacity(10).build();
        program.reserve(3);
        when(programRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(program));

        service.updateProgram(1L, new ProgramUpdateRequest("Updated Silence", null, 12));

        assertThat(program.getName()).isEqualTo("Updated Silence");
        assertThat(program.getCapacity()).isEqualTo(12);
        assertThat(program.getRemain()).isEqualTo(9);
    }

    @Test
    void 현재_신청_인원보다_정원을_작게_수정할_수_없다() {
        Program program = Program.builder().name("Morning Silence").capacity(10).build();
        program.reserve(3);
        when(programRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(program));

        assertThatThrownBy(() -> service.updateProgram(
                1L,
                new ProgramUpdateRequest("Updated Silence", null, 2)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 신청 인원보다 정원을 작게 변경할 수 없습니다.");
    }

    private User member(String email) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .name("테스트 회원")
                .phoneNumber("010-0000-0000")
                .role("USER")
                .build();
    }
}

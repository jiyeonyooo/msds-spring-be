package meditation_program.service;

import meditation_program.dto.ReservationRequest;
import meditation_program.entity.Program;
import meditation_program.entity.ProgramReservation;
import meditation_program.repository.ProgramRepository;
import meditation_program.repository.ProgramReservationRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramServiceTest {

    private final ProgramRepository programRepository = mock(ProgramRepository.class);
    private final ProgramReservationRepository reservationRepository = mock(ProgramReservationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProgramService service = new ProgramService(programRepository, reservationRepository, userRepository);

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

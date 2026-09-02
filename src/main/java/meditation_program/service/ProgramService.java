package meditation_program.service;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import meditation_program.dto.ProgramCreateRequest;
import meditation_program.dto.ProgramResponse;
import meditation_program.dto.ReservationRequest;
import meditation_program.entity.Program;
import meditation_program.entity.ProgramReservation;
import meditation_program.repository.ProgramRepository;
import meditation_program.repository.ProgramReservationRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public List<ProgramResponse> getPrograms() {
        return programRepository.findAll().stream()
                .map(ProgramResponse::from)
                .toList();
    }

    @Transactional
    public Long reserve(String userEmail, ReservationRequest request) {
        Program program = programRepository.findByIdForUpdate(request.programId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 프로그램입니다."));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        program.reserve(request.quantity()); // 재고 부족이면 여기서 예외

        ProgramReservation reservation = ProgramReservation.builder()
                .program(program).user(user).quantity(request.quantity())
                .build();
        return reservationRepository.save(reservation).getId();
    }

    @Transactional
    public void cancelReservation(String userEmail, Long reservationId) {
        ProgramReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
        if (!reservation.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("본인 예약만 취소할 수 있습니다.");
        }
        reservation.cancel();
        reservation.getProgram().cancelReservation(reservation.getQuantity());
    }

    // --- 관리자용 ---
    @Transactional
    public Long createProgram(ProgramCreateRequest request) {
        return programRepository.save(
                Program.builder().name(request.name())
                        .pictureUrl(request.pictureUrl())
                        .capacity(request.capacity())
                        .build()
        ).getId();
    }

    @Transactional
    public void deleteProgram(Long programId) {
        programRepository.deleteById(programId);
    }
}

package meditation_program.repository;

import meditation_program.entity.ProgramReservation;
import meditation_program.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramReservationRepository extends JpaRepository<ProgramReservation, Long> {
    List<ProgramReservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
}

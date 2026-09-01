package meditation_program.repository;

import meditation_program.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProgramReservation_Program_Id(Long programId);
    boolean existsByProgramReservationId(Long programReservationId);
}

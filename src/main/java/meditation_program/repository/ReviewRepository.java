package meditation_program.repository;

import meditation_program.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProgramReservationId(Long programReservationId);
    List<Review> findByProgramReservation_User_Email(String email);
}


package meditation_program.repository;

import meditation_program.entity.ProgramReservation;
import meditation_program.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramReservationRepository extends JpaRepository<ProgramReservation, Long> {
    List<ProgramReservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    boolean existsByProgramId(Long programId);

    @Query("select r from ProgramReservation r join fetch r.program join fetch r.user " +
            "where r.program.id = :programId order by r.createdAt desc")
    List<ProgramReservation> findApplicationsByProgramId(@Param("programId") Long programId);

    @Query("select r from ProgramReservation r join fetch r.program join fetch r.user " +
            "where lower(r.user.email) = lower(:email) order by r.createdAt desc")
    List<ProgramReservation> findAllByUserEmail(@Param("email") String email);
}

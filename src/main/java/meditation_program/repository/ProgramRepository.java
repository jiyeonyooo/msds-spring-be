package meditation_program.repository;

import jakarta.persistence.LockModeType;
import meditation_program.entity.Program;
import meditation_program.entity.ProgramStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Program p where p.id = :id")
    Optional<Program> findByIdForUpdate(@Param("id") Long id);
    List<Program> findByStatusNot(ProgramStatus status);

}

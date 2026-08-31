package resv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import resv.entity.Resv;
import resv.enums.ResvStatus;

import java.util.Optional;

public interface ResvRepository extends JpaRepository<Resv, Long> {
    Optional<Resv> findByResvNumber(String resvNumber);

    boolean existsByResvNumber(String resvNumber);

    boolean existsByRoomUnitsIdAndResvStatus(Long roomUnitsId, ResvStatus resvStatus);
}

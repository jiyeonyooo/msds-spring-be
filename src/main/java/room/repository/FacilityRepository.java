package room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import room.entity.Facility;
import room.entity.enums.FacilityCategory;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findAllByActiveTrueOrderByCategoryAscNameAsc();

    List<Facility> findAllByCategoryAndActiveTrueOrderByNameAsc(FacilityCategory category);
}

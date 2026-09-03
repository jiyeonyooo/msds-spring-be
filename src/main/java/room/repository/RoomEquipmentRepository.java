package room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import room.entity.RoomEquipment;

import java.util.List;

public interface RoomEquipmentRepository extends JpaRepository<RoomEquipment, Long> {
    List<RoomEquipment> findAllByActiveTrueOrderByCategoryAscNameAsc();

    boolean existsByName(String name);
}

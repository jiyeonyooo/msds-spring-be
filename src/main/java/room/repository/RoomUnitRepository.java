package room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import room.entity.RoomUnit;

public interface RoomUnitRepository extends JpaRepository<RoomUnit, Long> {

    boolean existsByRoomIdAndRoomNumber(Long roomId, String roomNumber);
}

package room.repository;

import room.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @EntityGraph(attributePaths = {
            "equipmentMappings",
            "equipmentMappings.equipment"
    })
    @Query("select r from Room r where r.id = :roomId")
    Optional<Room> findDetailById(@Param("roomId") Long roomId);
}

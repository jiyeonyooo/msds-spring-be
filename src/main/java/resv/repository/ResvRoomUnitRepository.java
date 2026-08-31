package resv.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import resv.enums.ResvStatus;
import room.entity.RoomUnit;
import room.entity.enums.RoomUnitStatus;

import java.time.LocalDate;
import java.util.List;

public interface ResvRoomUnitRepository extends JpaRepository<RoomUnit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select roomUnit from RoomUnit roomUnit
            where roomUnit.room.id = :roomId
              and roomUnit.status = :roomUnitStatus
              and not exists (
                  select resv from Resv resv
                  where resv.roomUnitsId = roomUnit.id
                    and resv.resvStatus = :resvStatus
                    and resv.checkInDate < :checkOutDate
                    and resv.checkOutDate > :checkInDate
              )
            order by roomUnit.id asc
            """)
    List<RoomUnit> findAvailableForUpdate(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomUnitStatus") RoomUnitStatus roomUnitStatus,
            @Param("resvStatus") ResvStatus resvStatus);
}

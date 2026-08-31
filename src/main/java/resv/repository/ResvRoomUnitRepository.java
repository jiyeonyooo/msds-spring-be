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

    interface RoomAvailabilityProjection {
        Long getRoomId();
        String getRoomName();
        Integer getMaxGuests();
        Integer getBasePrice();
        long getRemainingCount();
    }

    @Query("""
            select room.id as roomId, room.name as roomName, room.maxGuests as maxGuests,
                   room.basePrice as basePrice, count(roomUnit) as remainingCount
            from Room room
            left join RoomUnit roomUnit on roomUnit.room = room
                and roomUnit.status = :roomUnitStatus
                and not exists (
                    select resv from Resv resv
                    where resv.roomUnitsId = roomUnit.id
                      and resv.resvStatus = :resvStatus
                      and resv.checkInDate < :checkOutDate
                      and resv.checkOutDate > :checkInDate
                )
            where room.maxGuests >= :guestCount
            group by room.id, room.name, room.maxGuests, room.basePrice
            order by room.id asc
            """)
    List<RoomAvailabilityProjection> findAvailability(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("guestCount") Integer guestCount,
            @Param("roomUnitStatus") RoomUnitStatus roomUnitStatus,
            @Param("resvStatus") ResvStatus resvStatus);

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

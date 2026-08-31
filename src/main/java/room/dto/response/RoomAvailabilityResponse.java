package room.dto.response;

import java.time.LocalDate;

public record RoomAvailabilityResponse(
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        Boolean available,
        Integer remainingRooms,
        PriceResponse price
) {}

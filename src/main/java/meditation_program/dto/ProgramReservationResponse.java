package meditation_program.dto;

import meditation_program.entity.ProgramReservation;

import java.time.LocalDateTime;

public record ProgramReservationResponse(
        Long reservationId,
        Long programId,
        String programName,
        String pictureUrl,
        int quantity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public static ProgramReservationResponse from(ProgramReservation reservation) {
        return new ProgramReservationResponse(
                reservation.getId(),
                reservation.getProgram().getId(),
                reservation.getProgram().getName(),
                reservation.getProgram().getPictureUrl(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getCancelledAt()
        );
    }
}

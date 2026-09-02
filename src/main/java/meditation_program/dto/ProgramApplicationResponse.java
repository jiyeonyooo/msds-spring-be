package meditation_program.dto;

import meditation_program.entity.ProgramReservation;

import java.time.LocalDateTime;

public record ProgramApplicationResponse(
        Long reservationId,
        Long programId,
        Long userId,
        String name,
        String email,
        int quantity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public static ProgramApplicationResponse from(ProgramReservation reservation) {
        return new ProgramApplicationResponse(
                reservation.getId(),
                reservation.getProgram().getId(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getUser().getEmail(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getCancelledAt()
        );
    }
}

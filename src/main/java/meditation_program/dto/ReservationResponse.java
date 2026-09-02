package meditation_program.dto;

import meditation_program.entity.ProgramReservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        String programName,
        int quantity,
        String status,
        LocalDateTime createdAt,
        boolean hasReview
) {
    public static ReservationResponse from(ProgramReservation r, boolean hasReview) {
        return new ReservationResponse(
                r.getId(),
                r.getProgram().getName(),
                r.getQuantity(),
                r.getStatus().name(),
                r.getCreatedAt(),
                hasReview
        );
    }
}
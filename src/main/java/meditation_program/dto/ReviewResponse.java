package meditation_program.dto;

import meditation_program.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long programReservationId, Long userId,
                             String programName, String userName,
                             String content, LocalDateTime createdAt) {
    public static ReviewResponse from(Review r) {
        var resv = r.getProgramReservation();
        return new ReviewResponse(r.getId(), resv.getId(), resv.getUser().getId(),
                resv.getProgram().getName(),
                resv.getUser().getName(), r.getContent(), r.getCreatedAt());
    }
}

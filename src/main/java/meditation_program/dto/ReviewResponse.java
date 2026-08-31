package meditation_program.dto;

import meditation_program.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, String programName, String memberName,
                             String content, LocalDateTime createdAt) {
    public static ReviewResponse from(Review r) {
        var resv = r.getProgramReservation();
        return new ReviewResponse(r.getId(), resv.getProgram().getName(),
                resv.getMember().getName(), r.getContent(), r.getCreatedAt());
    }
}

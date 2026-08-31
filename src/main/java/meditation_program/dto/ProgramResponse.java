package meditation_program.dto;

import meditation_program.entity.Program;

public record ProgramResponse(Long id, String name, String pictureUrl,
                              int capacity, int remain, String status) {
    public static ProgramResponse from(Program p) {
        return new ProgramResponse(p.getId(), p.getName(), p.getPictureUrl(),
                p.getCapacity(), p.getRemain(), p.getStatus().name());
    }
}
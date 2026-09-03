package meditation_program.config;

import meditation_program.entity.Program;
import meditation_program.repository.ProgramRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.program.demo", name = "enabled", havingValue = "true")
public class ProgramDemoDataInitializer implements ApplicationRunner {

    private final ProgramRepository programRepository;

    public ProgramDemoDataInitializer(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (programRepository.count() > 0) {
            return;
        }

        programRepository.saveAll(List.of(
                program("Morning Silence Meditation", 12),
                program("Ocean Breathing", 10),
                program("Slow Walking", 8),
                program("Evening Tea Meditation", 10)
        ));
    }

    private Program program(String name, int capacity) {
        return Program.builder()
                .name(name)
                .capacity(capacity)
                .build();
    }
}

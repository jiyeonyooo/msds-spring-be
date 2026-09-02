package meditation_program.config;

import meditation_program.entity.Program;
import meditation_program.entity.ProgramStatus;
import meditation_program.repository.ProgramRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramDemoDataInitializerTest {

    private final ProgramRepository programRepository = mock(ProgramRepository.class);
    private final ProgramDemoDataInitializer initializer = new ProgramDemoDataInitializer(programRepository);

    @Test
    void 프로그램이_없으면_로컬_연동용_프로그램을_생성한다() {
        when(programRepository.count()).thenReturn(0L);

        initializer.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Program>> captor = ArgumentCaptor.forClass(List.class);
        verify(programRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .hasSize(4)
                .extracting(Program::getName)
                .containsExactly(
                        "Morning Silence Meditation",
                        "Ocean Breathing",
                        "Slow Walking",
                        "Evening Tea Meditation"
                );
        assertThat(captor.getValue())
                .allSatisfy(program -> {
                    assertThat(program.getRemain()).isEqualTo(program.getCapacity());
                    assertThat(program.getStatus()).isEqualTo(ProgramStatus.OPEN);
                });
    }

    @Test
    void 프로그램이_있으면_기존_데이터를_보존한다() {
        when(programRepository.count()).thenReturn(1L);

        initializer.run(new DefaultApplicationArguments());

        verify(programRepository, never()).saveAll(any());
    }
}

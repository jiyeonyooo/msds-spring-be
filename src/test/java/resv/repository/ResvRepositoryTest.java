package resv.repository;

import com.example.meditation.MeditationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import resv.entity.Resv;
import resv.enums.ResvStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MeditationApplication.class)
class ResvRepositoryTest {

    @Autowired
    private ResvRepository resvRepository;

    @Test
    void persistsReservationWithDocumentedMappings() {
        Resv saved = resvRepository.saveAndFlush(Resv.builder()
                .roomUnitsId(101L)
                .memberId(10L)
                .resvNumber("RSV-20260910-A13F59C821D04E7B")
                .checkInDate(LocalDate.of(2026, 9, 10))
                .checkOutDate(LocalDate.of(2026, 9, 12))
                .guestCount(2)
                .pricePerNight(80_000)
                .totalPrice(160_000)
                .resvStatus(ResvStatus.RESERVED)
                .build());

        assertThat(resvRepository.findByResvNumber(saved.getResvNumber()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getRoomUnitsId()).isEqualTo(101L);
                    assertThat(found.getMemberId()).isEqualTo(10L);
                    assertThat(found.getResvStatus()).isEqualTo(ResvStatus.RESERVED);
                    assertThat(found.getCreatedAt()).isNotNull();
                });
    }
}

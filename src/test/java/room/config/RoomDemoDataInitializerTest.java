package room.config;

import com.example.meditation.MeditationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import room.repository.FacilityRepository;
import room.repository.RoomRepository;
import room.repository.RoomEquipmentRepository;
import room.repository.RoomUnitRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MeditationApplication.class)
class RoomDemoDataInitializerTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomUnitRepository roomUnitRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private RoomEquipmentRepository roomEquipmentRepository;

    @Test
    void 객실과_실제_객실과_시설을_중복없이_생성한다() {
        RoomDemoDataInitializer initializer = new RoomDemoDataInitializer(
                roomRepository,
                roomUnitRepository,
                facilityRepository,
                roomEquipmentRepository
        );

        initializer.run(new DefaultApplicationArguments());
        initializer.run(new DefaultApplicationArguments());

        assertThat(roomRepository.findAll())
                .extracting("name")
                .containsExactlyInAnyOrder("Ocean Silence Suite", "Forest Twin", "Still Room");
        assertThat(roomUnitRepository.findAll()).hasSize(6);
        assertThat(facilityRepository.findAll()).hasSize(6);
        assertThat(roomEquipmentRepository.findAll()).hasSize(5);
    }
}

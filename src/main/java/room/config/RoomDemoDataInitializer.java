package room.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import room.entity.Facility;
import room.entity.Room;
import room.entity.RoomEquipment;
import room.entity.RoomUnit;
import room.entity.enums.EquipmentCategory;
import room.entity.enums.FacilityCategory;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import room.entity.enums.RoomUnitStatus;
import room.repository.FacilityRepository;
import room.repository.RoomRepository;
import room.repository.RoomEquipmentRepository;
import room.repository.RoomUnitRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.room.demo", name = "enabled", havingValue = "true")
public class RoomDemoDataInitializer implements ApplicationRunner {

    private final RoomRepository roomRepository;
    private final RoomUnitRepository roomUnitRepository;
    private final FacilityRepository facilityRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Room> rooms = roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getName, Function.identity(), (first, ignored) -> first));

        Room ocean = rooms.computeIfAbsent("Ocean Silence Suite", ignored -> saveRoom(
                "Ocean Silence Suite",
                "바다와 하늘이 가장 고요하게 만나는 방",
                RoomType.STAY,
                2,
                2,
                "38.00",
                180000
        ));
        Room forest = rooms.computeIfAbsent("Forest Twin", ignored -> saveRoom(
                "Forest Twin",
                "숲의 결을 따라 쉬어가는 트윈룸",
                RoomType.REST,
                2,
                2,
                "32.00",
                140000
        ));
        Room still = rooms.computeIfAbsent("Still Room", ignored -> saveRoom(
                "Still Room",
                "정원을 바라보며 혼자 머무는 명상 객실",
                RoomType.MEDITATE,
                1,
                1,
                "24.00",
                110000
        ));

        createUnitIfMissing(ocean, "201", 2);
        createUnitIfMissing(ocean, "202", 2);
        createUnitIfMissing(forest, "301", 3);
        createUnitIfMissing(forest, "302", 3);
        createUnitIfMissing(still, "101", 1);
        createUnitIfMissing(still, "102", 1);

        createFacilitiesIfMissing();
        createEquipmentsIfMissing();
    }

    private Room saveRoom(
            String name,
            String description,
            RoomType roomType,
            int standardGuests,
            int maxGuests,
            String area,
            int basePrice
    ) {
        return roomRepository.save(Room.create(
                name,
                description,
                roomType,
                RoomStatus.AVAILABLE,
                standardGuests,
                maxGuests,
                new BigDecimal(area),
                basePrice
        ));
    }

    private void createUnitIfMissing(Room room, String roomNumber, int floor) {
        if (roomUnitRepository.existsByRoomIdAndRoomNumber(room.getId(), roomNumber)) {
            return;
        }
        roomUnitRepository.save(RoomUnit.create(room, roomNumber, floor, RoomUnitStatus.ACTIVE));
    }

    private void createFacilitiesIfMissing() {
        List<FacilitySeed> seeds = List.of(
                new FacilitySeed("마음쉼 명상실", FacilityCategory.WELLNESS, "호흡과 명상 프로그램을 위한 고요한 공간"),
                new FacilitySeed("정원 산책로", FacilityCategory.LEISURE, "천천히 걸으며 계절의 흐름을 느끼는 산책로"),
                new FacilitySeed("티 라운지", FacilityCategory.FOOD, "따뜻한 차와 함께 머무는 공용 라운지"),
                new FacilitySeed("마음 기록실", FacilityCategory.WELLNESS, "오늘의 마음을 차분히 기록하는 공간"),
                new FacilitySeed("공용 주방", FacilityCategory.CONVENIENCE, "간단한 식사와 음료를 준비할 수 있는 주방"),
                new FacilitySeed("전용 주차장", FacilityCategory.PARKING, "투숙객이 이용할 수 있는 무료 주차 공간")
        );

        for (FacilitySeed seed : seeds) {
            if (!facilityRepository.existsByName(seed.name())) {
                facilityRepository.save(Facility.create(
                        seed.name(),
                        seed.category(),
                        seed.description(),
                        null,
                        true
                ));
            }
        }
    }

    private void createEquipmentsIfMissing() {
        List<EquipmentSeed> seeds = List.of(
                new EquipmentSeed("무선 인터넷", EquipmentCategory.CONVENIENCE, "객실 전용 Wi-Fi"),
                new EquipmentSeed("냉난방기", EquipmentCategory.ELECTRONICS, "개별 온도 조절"),
                new EquipmentSeed("호텔 침구", EquipmentCategory.BEDDING, "편안한 숙면을 위한 침구"),
                new EquipmentSeed("차 세트", EquipmentCategory.KITCHEN, "차와 온수 포트"),
                new EquipmentSeed("명상 쿠션", EquipmentCategory.WELLNESS, "객실 내 명상용 쿠션")
        );

        for (EquipmentSeed seed : seeds) {
            if (!roomEquipmentRepository.existsByName(seed.name())) {
                roomEquipmentRepository.save(RoomEquipment.create(
                        seed.name(), seed.category(), seed.description(), null
                ));
            }
        }
    }

    private record FacilitySeed(String name, FacilityCategory category, String description) {
    }

    private record EquipmentSeed(String name, EquipmentCategory category, String description) {
    }
}

package room.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import room.dto.request.RoomImageCreateRequest;
import room.dto.response.RoomImageResponse;
import room.entity.Room;
import room.entity.enums.RoomImageType;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import room.repository.RoomRepository;
import room.repository.RoomEquipmentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoomImageServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomEquipmentRepository roomEquipmentRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void addsImagesAndUpdatesMainImageUrl() {
        Room room = room();
        given(roomRepository.findDetailById(1L)).willReturn(Optional.of(room));

        List<RoomImageResponse> result = roomService.addRoomImages(1L, List.of(
                new RoomImageCreateRequest("/uploads/rooms/main.webp", RoomImageType.MAIN, 0),
                new RoomImageCreateRequest("/uploads/rooms/view.webp", RoomImageType.VIEW, 1)
        ));

        assertThat(result).hasSize(2);
        assertThat(room.getMainImageUrl()).isEqualTo("/uploads/rooms/main.webp");
        assertThat(room.getImages()).extracting(image -> image.getImageType())
                .containsExactly(RoomImageType.MAIN, RoomImageType.VIEW);
        verify(roomRepository).saveAndFlush(room);
    }

    @Test
    void rejectsMoreThanOneMainImage() {
        Room room = room();
        given(roomRepository.findDetailById(1L)).willReturn(Optional.of(room));

        assertThatThrownBy(() -> roomService.addRoomImages(1L, List.of(
                new RoomImageCreateRequest("/uploads/rooms/a.webp", RoomImageType.MAIN, 0),
                new RoomImageCreateRequest("/uploads/rooms/b.webp", RoomImageType.MAIN, 1)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only one main image");
    }

    private Room room() {
        return Room.create(
                "Deep Rest",
                "A quiet room",
                RoomType.REST,
                RoomStatus.AVAILABLE,
                1,
                2,
                BigDecimal.valueOf(31.5),
                180_000
        );
    }
}

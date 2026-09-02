package room.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import room.dto.request.RoomCreateRequest;
import room.dto.request.RoomUpdateRequest;
import room.dto.response.RoomDetailResponse;
import room.dto.type.RoomImageType;
import room.entity.Room;
import room.entity.enums.BedType;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import room.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    private final RoomRepository roomRepository = mock(RoomRepository.class);
    private final RoomService service = new RoomService(roomRepository);

    @Test
    void 객실을_등록할_때_대표이미지와_침대정보를_저장한다() {
        RoomCreateRequest request = new RoomCreateRequest(
                "마음쉼 스위트",
                "조용한 객실",
                RoomType.RETREAT,
                RoomStatus.AVAILABLE,
                1,
                2,
                new BigDecimal("32.50"),
                280000,
                "https://example.com/room.jpg",
                BedType.QUEEN,
                1
        );
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomDetailResponse result = service.createRoom(request);

        assertThat(result.roomSpecs().bedType()).isEqualTo(BedType.QUEEN);
        assertThat(result.roomSpecs().bedCount()).isEqualTo(1);
        assertThat(result.images()).singleElement().satisfies(image -> {
            assertThat(image.imageType()).isEqualTo(RoomImageType.MAIN);
            assertThat(image.imageUrl()).isEqualTo("https://example.com/room.jpg");
        });
    }

    @Test
    void 객실_대표이미지와_침대정보를_부분수정한다() {
        Room room = Room.create(
                "기존 객실",
                "기존 설명",
                RoomType.STAY,
                RoomStatus.AVAILABLE,
                1,
                2,
                new BigDecimal("20.00"),
                180000
        );
        ReflectionTestUtils.setField(room, "id", 1L);
        when(roomRepository.findDetailById(1L)).thenReturn(Optional.of(room));
        RoomUpdateRequest request = new RoomUpdateRequest(
                null, null, null, null, null, null, null, null,
                "https://example.com/updated.jpg", BedType.KING, 1
        );

        RoomDetailResponse result = service.updateRoom(1L, request);

        assertThat(result.roomSpecs().bedType()).isEqualTo(BedType.KING);
        assertThat(result.images()).singleElement()
                .extracting("imageUrl")
                .isEqualTo("https://example.com/updated.jpg");
    }
}

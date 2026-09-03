package room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import room.entity.enums.RoomImageType;

@Entity
@Table(name = "room_images", indexes = {
        @Index(name = "idx_room_images_room_sort", columnList = "room_id, sort_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private RoomImageType imageType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static RoomImage create(
            Room room,
            String imageUrl,
            RoomImageType imageType,
            Integer sortOrder
    ) {
        RoomImage image = new RoomImage();
        image.room = room;
        image.imageUrl = imageUrl;
        image.imageType = imageType;
        image.sortOrder = sortOrder;
        return image;
    }
}

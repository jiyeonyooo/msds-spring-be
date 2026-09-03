package room.entity;

import room.entity.enums.BedType;
import room.entity.enums.RoomStatus;
import room.entity.enums.RoomType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_rooms_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    public static Room create(
            String name,
            String description,
            RoomType roomType,
            RoomStatus status,
            Integer standardGuests,
            Integer maxGuests,
            BigDecimal areaM2,
            Integer basePrice
    ) {
        return create(
                name,
                description,
                roomType,
                status,
                standardGuests,
                maxGuests,
                areaM2,
                basePrice,
                null,
                null,
                null
        );
    }

    public static Room create(
            String name,
            String description,
            RoomType roomType,
            RoomStatus status,
            Integer standardGuests,
            Integer maxGuests,
            BigDecimal areaM2,
            Integer basePrice,
            String mainImageUrl,
            BedType bedType,
            Integer bedCount
    ) {
        Room room = new Room();
        room.name = name;
        room.description = description;
        room.roomType = roomType;
        room.status = status;
        room.standardGuests = standardGuests;
        room.maxGuests = maxGuests;
        room.areaM2 = areaM2;
        room.basePrice = basePrice;
        room.mainImageUrl = mainImageUrl;
        room.bedType = bedType;
        room.bedCount = bedCount;
        return room;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "main_image_url", length = 512)
    private String mainImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "standard_guests", nullable = false)
    private Integer standardGuests;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    @Column(name = "area_m2", precision = 6, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", length = 20)
    private BedType bedType;

    @Column(name = "bed_count")
    private Integer bedCount;

    @OneToMany(mappedBy = "room")
    private List<RoomUnit> roomUnits = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomEquipmentMapping> equipmentMappings = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RoomImage> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(
            String name,
            String description,
            RoomType roomType,
            RoomStatus status,
            Integer standardGuests,
            Integer maxGuests,
            BigDecimal areaM2,
            Integer basePrice
    ) {
        update(
                name,
                description,
                roomType,
                status,
                standardGuests,
                maxGuests,
                areaM2,
                basePrice,
                null,
                null,
                null
        );
    }

    public void update(
            String name,
            String description,
            RoomType roomType,
            RoomStatus status,
            Integer standardGuests,
            Integer maxGuests,
            BigDecimal areaM2,
            Integer basePrice,
            String mainImageUrl,
            BedType bedType,
            Integer bedCount
    ) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (roomType != null) {
            this.roomType = roomType;
        }
        if (status != null) {
            this.status = status;
        }
        if (standardGuests != null) {
            this.standardGuests = standardGuests;
        }
        if (maxGuests != null) {
            this.maxGuests = maxGuests;
        }
        if (areaM2 != null) {
            this.areaM2 = areaM2;
        }
        if (basePrice != null) {
            this.basePrice = basePrice;
        }
        if (mainImageUrl != null) {
            this.mainImageUrl = mainImageUrl;
        }
        if (bedType != null) {
            this.bedType = bedType;
        }
        if (bedCount != null) {
            this.bedCount = bedCount;
        }
    }

    public void replaceEquipmentMappings(List<RoomEquipmentMapping> mappings) {
        equipmentMappings.clear();
        equipmentMappings.addAll(mappings);
    }

    public void addImage(RoomImage image) {
        images.add(image);
    }

    public void updateMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

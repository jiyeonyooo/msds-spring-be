package room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_equipment_mappings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_equipment_mappings_room_equipment", columnNames = {"room_id", "equipment_id"})
        },
        indexes = {
                @Index(name = "idx_room_equipment_mappings_room", columnList = "room_id"),
                @Index(name = "idx_room_equipment_mappings_equipment", columnList = "equipment_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomEquipmentMapping {

    public static RoomEquipmentMapping create(
            Room room,
            RoomEquipment equipment,
            Integer quantity,
            String note
    ) {
        RoomEquipmentMapping mapping = new RoomEquipmentMapping();
        mapping.room = room;
        mapping.equipment = equipment;
        mapping.quantity = quantity;
        mapping.note = note;
        return mapping;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private RoomEquipment equipment;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer quantity = 1;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (quantity == null) {
            quantity = 1;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

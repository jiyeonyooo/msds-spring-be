package room.entity;

import room.entity.enums.FacilityCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "facilities", indexes = {
        @Index(name = "idx_facilities_category_active", columnList = "category, active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility {

    public static Facility create(
            String name,
            FacilityCategory category,
            String description,
            String imageUrl,
            Boolean active
    ) {
        Facility facility = new Facility();
        facility.name = name;
        facility.category = category;
        facility.description = description;
        facility.imageUrl = imageUrl;
        facility.active = active == null || active;
        return facility;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FacilityCategory category;

    @Column(length = 255)
    private String description;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(
            String name,
            FacilityCategory category,
            String description,
            String imageUrl,
            Boolean active
    ) {
        if (name != null) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
        if (description != null) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (active != null) {
            this.active = active;
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

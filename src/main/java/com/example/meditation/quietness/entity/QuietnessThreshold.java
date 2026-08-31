package com.example.meditation.quietness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quietness_thresholds", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quietness_threshold_level", columnNames = {"guesthouse_id", "level"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuietnessThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guesthouse_id", nullable = false)
    private Long guesthouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuietnessLevel level;

    @Column(name = "min_decibel", precision = 5, scale = 2)
    private BigDecimal minDecibel;

    @Column(name = "max_decibel", precision = 5, scale = 2)
    private BigDecimal maxDecibel;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean includes(BigDecimal value) {
        boolean aboveMinimum = minDecibel == null || value.compareTo(minDecibel) >= 0;
        boolean belowMaximum = maxDecibel == null || value.compareTo(maxDecibel) <= 0;
        return aboveMinimum && belowMaximum;
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

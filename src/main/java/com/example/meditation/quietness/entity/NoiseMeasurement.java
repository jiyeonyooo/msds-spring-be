package com.example.meditation.quietness.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "noise_measurements", indexes = {
        @Index(name = "idx_noise_measurement_device_time", columnList = "device_id, measured_at"),
        @Index(name = "idx_noise_measurement_space_time", columnList = "space_id, measured_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoiseMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private NoiseDevice device;

    @Column(name = "guesthouse_id", nullable = false)
    private Long guesthouseId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal decibel;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

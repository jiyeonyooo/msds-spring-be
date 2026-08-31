package com.example.meditation.quietness.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "noise_devices", indexes = {
        @Index(name = "idx_noise_device_space", columnList = "space_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoiseDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guesthouse_id", nullable = false)
    private Long guesthouseId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoiseDeviceStatus status;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public NoiseDevice(
            Long guesthouseId,
            Long spaceId,
            String deviceName,
            String serialNumber,
            String modelName,
            NoiseDeviceStatus status
    ) {
        this.guesthouseId = guesthouseId;
        this.spaceId = spaceId;
        this.deviceName = deviceName;
        this.serialNumber = serialNumber;
        this.modelName = modelName;
        this.status = status;
        this.installedAt = LocalDateTime.now();
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

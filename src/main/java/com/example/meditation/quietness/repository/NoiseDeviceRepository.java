package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.NoiseDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoiseDeviceRepository extends JpaRepository<NoiseDevice, Long> {
    List<NoiseDevice> findAllByGuesthouseId(Long guesthouseId);
    boolean existsBySerialNumber(String serialNumber);
}

package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.QuietSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuietSpaceRepository extends JpaRepository<QuietSpace, Long> {
    List<QuietSpace> findAllByGuesthouseIdOrderByIdAsc(Long guesthouseId);

    boolean existsByGuesthouseIdAndName(Long guesthouseId, String name);
}

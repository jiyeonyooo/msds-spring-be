package com.example.meditation.quietness.repository;

import com.example.meditation.quietness.entity.QuietnessThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuietnessThresholdRepository extends JpaRepository<QuietnessThreshold, Long> {
    List<QuietnessThreshold> findAllByGuesthouseIdOrderByDisplayOrderAsc(Long guesthouseId);

    boolean existsByGuesthouseId(Long guesthouseId);
}

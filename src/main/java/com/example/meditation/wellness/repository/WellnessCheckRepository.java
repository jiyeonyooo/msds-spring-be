package com.example.meditation.wellness.repository;

import com.example.meditation.wellness.entity.WellnessCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WellnessCheckRepository extends JpaRepository<WellnessCheck, Long> {
    List<WellnessCheck> findAllByMemberIdOrderByCheckedAtDesc(Long memberId);
    Optional<WellnessCheck> findByIdAndMemberId(Long id, Long memberId);
}

package com.example.meditation.wellness.repository;

import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WellnessAnswerRepository extends JpaRepository<WellnessAnswer, Long> {
    List<WellnessAnswer> findAllByWellnessCheckOrderByWellnessQuestionDisplayOrderAsc(
            WellnessCheck wellnessCheck
    );

    List<WellnessAnswer> findAllByWellnessCheckIn(List<WellnessCheck> wellnessChecks);
}

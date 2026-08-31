package com.example.meditation.wellness.repository;

import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WellnessSurveyRepository extends JpaRepository<WellnessSurvey, Long> {
    Optional<WellnessSurvey> findFirstByStatusOrderByVersionDesc(SurveyStatus status);
}

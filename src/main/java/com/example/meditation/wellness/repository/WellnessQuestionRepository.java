package com.example.meditation.wellness.repository;

import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WellnessQuestionRepository extends JpaRepository<WellnessQuestion, Long> {
    List<WellnessQuestion> findAllBySurveyAndStatusOrderByDisplayOrderAsc(
            WellnessSurvey survey,
            QuestionStatus status
    );
}

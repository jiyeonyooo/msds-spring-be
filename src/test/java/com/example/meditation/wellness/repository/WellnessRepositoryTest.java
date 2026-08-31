package com.example.meditation.wellness.repository;

import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessCheck;
import com.example.meditation.wellness.entity.WellnessLevel;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WellnessRepositoryTest {

    @Autowired
    private WellnessSurveyRepository surveyRepository;

    @Autowired
    private WellnessQuestionRepository questionRepository;

    @Autowired
    private WellnessCheckRepository checkRepository;

    @Autowired
    private WellnessAnswerRepository answerRepository;

    @Test
    void 검사_답변을_문항_표시순서대로_조회한다() {
        WellnessSurvey survey = surveyRepository.save(
                new WellnessSurvey("테스트 설문", 99, SurveyStatus.ACTIVE)
        );
        WellnessQuestion second = questionRepository.save(question(survey, 2));
        WellnessQuestion first = questionRepository.save(question(survey, 1));
        WellnessCheck check = checkRepository.save(new WellnessCheck(
                10L, null, survey, StayStage.GENERAL, 50, WellnessLevel.NORMAL
        ));
        answerRepository.saveAll(List.of(
                new WellnessAnswer(check, second, 2, 2),
                new WellnessAnswer(check, first, 1, 1)
        ));

        List<WellnessAnswer> answers =
                answerRepository.findAllByWellnessCheckOrderByWellnessQuestionDisplayOrderAsc(check);

        assertThat(answers)
                .extracting(answer -> answer.getWellnessQuestion().getDisplayOrder())
                .containsExactly(1, 2);
    }

    private WellnessQuestion question(WellnessSurvey survey, int order) {
        return new WellnessQuestion(
                survey,
                WellnessCategory.STRESS,
                "질문 " + order,
                order,
                false,
                QuestionStatus.ACTIVE
        );
    }
}

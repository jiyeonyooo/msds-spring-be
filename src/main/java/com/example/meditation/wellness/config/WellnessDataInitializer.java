package com.example.meditation.wellness.config;

import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import com.example.meditation.wellness.repository.WellnessQuestionRepository;
import com.example.meditation.wellness.repository.WellnessSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
public class WellnessDataInitializer implements ApplicationRunner {

    private final WellnessSurveyRepository surveyRepository;
    private final WellnessQuestionRepository questionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (surveyRepository.findFirstByStatusOrderByVersionDesc(SurveyStatus.ACTIVE).isPresent()) {
            return;
        }

        WellnessSurvey survey = surveyRepository.save(
                new WellnessSurvey("오늘의 마음상태 체크", 1, SurveyStatus.ACTIVE)
        );

        questionRepository.saveAll(List.of(
                question(survey, WellnessCategory.STRESS,
                        "지금 해야 할 일이나 걱정 때문에 마음이 무겁게 느껴진다.", 1, false),
                question(survey, WellnessCategory.STRESS,
                        "사소한 일에도 쉽게 예민해지거나 짜증이 난다.", 2, false),
                question(survey, WellnessCategory.TENSION,
                        "몸이나 마음에 긴장이 남아 있다고 느낀다.", 3, false),
                question(survey, WellnessCategory.FATIGUE,
                        "충분히 쉬어도 피로가 남아 있는 것 같다.", 4, false),
                question(survey, WellnessCategory.FATIGUE,
                        "일상적인 활동을 하기에도 에너지가 부족하다고 느낀다.", 5, false),
                question(survey, WellnessCategory.REST,
                        "지금 마음이 편안하고 안정되어 있다고 느낀다.", 6, true),
                question(survey, WellnessCategory.REST,
                        "현재 충분히 쉬고 있다는 느낌이 든다.", 7, true),
                question(survey, WellnessCategory.MOOD,
                        "오늘 전반적인 기분이 긍정적이다.", 8, true),
                question(survey, WellnessCategory.FOCUS,
                        "여러 생각이 떠올라 마음을 편하게 쉬기 어렵다.", 9, false),
                question(survey, WellnessCategory.OVERALL,
                        "지금 당장 잠시 멈추고 쉬고 싶다는 생각이 든다.", 10, false)
        ));
    }

    private WellnessQuestion question(
            WellnessSurvey survey,
            WellnessCategory category,
            String content,
            int displayOrder,
            boolean reverseScored
    ) {
        return new WellnessQuestion(
                survey,
                category,
                content,
                displayOrder,
                reverseScored,
                QuestionStatus.ACTIVE
        );
    }
}

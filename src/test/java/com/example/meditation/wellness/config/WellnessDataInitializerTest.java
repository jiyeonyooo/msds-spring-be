package com.example.meditation.wellness.config;

import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import com.example.meditation.wellness.repository.WellnessQuestionRepository;
import com.example.meditation.wellness.repository.WellnessSurveyRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WellnessDataInitializerTest {

    private final WellnessSurveyRepository surveyRepository = mock(WellnessSurveyRepository.class);
    private final WellnessQuestionRepository questionRepository = mock(WellnessQuestionRepository.class);
    private final WellnessDataInitializer initializer =
            new WellnessDataInitializer(surveyRepository, questionRepository);

    @Test
    void 활성_설문이_없으면_설문과_열개_문항을_생성한다() {
        WellnessSurvey savedSurvey = new WellnessSurvey("오늘의 마음상태 체크", 1, SurveyStatus.ACTIVE);
        when(surveyRepository.findFirstByStatusOrderByVersionDesc(SurveyStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(surveyRepository.save(any(WellnessSurvey.class))).thenReturn(savedSurvey);

        initializer.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WellnessQuestion>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(10);
        assertThat(captor.getValue()).filteredOn(WellnessQuestion::isReverseScored).hasSize(3);
    }

    @Test
    void 활성_설문이_있으면_초기_데이터를_중복_생성하지_않는다() {
        WellnessSurvey survey = new WellnessSurvey("기존 설문", 1, SurveyStatus.ACTIVE);
        when(surveyRepository.findFirstByStatusOrderByVersionDesc(SurveyStatus.ACTIVE))
                .thenReturn(Optional.of(survey));

        initializer.run(new DefaultApplicationArguments());

        verify(surveyRepository, never()).save(any(WellnessSurvey.class));
        verify(questionRepository, never()).saveAll(any());
    }
}

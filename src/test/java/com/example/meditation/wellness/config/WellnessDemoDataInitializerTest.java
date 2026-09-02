package com.example.meditation.wellness.config;

import com.example.meditation.wellness.repository.WellnessAnswerRepository;
import com.example.meditation.wellness.repository.WellnessCheckRepository;
import com.example.meditation.wellness.repository.WellnessQuestionRepository;
import com.example.meditation.wellness.repository.WellnessSurveyRepository;
import com.example.meditation.wellness.service.WellnessScoringService;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WellnessDemoDataInitializerTest {

    @Test
    void 기존_검사기록이_있으면_데모데이터를_중복생성하지_않는다() {
        WellnessSurveyRepository surveyRepository = mock(WellnessSurveyRepository.class);
        WellnessQuestionRepository questionRepository = mock(WellnessQuestionRepository.class);
        WellnessCheckRepository checkRepository = mock(WellnessCheckRepository.class);
        WellnessAnswerRepository answerRepository = mock(WellnessAnswerRepository.class);
        WellnessScoringService scoringService = mock(WellnessScoringService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(checkRepository.count()).thenReturn(1L);
        WellnessDemoDataInitializer initializer = new WellnessDemoDataInitializer(
                surveyRepository,
                questionRepository,
                checkRepository,
                answerRepository,
                scoringService,
                userRepository,
                passwordEncoder
        );

        initializer.run(new DefaultApplicationArguments());

        verify(surveyRepository, never()).findFirstByStatusOrderByVersionDesc(org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}

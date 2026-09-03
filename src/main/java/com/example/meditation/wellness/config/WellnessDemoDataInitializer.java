package com.example.meditation.wellness.config;

import com.example.meditation.wellness.dto.request.WellnessAnswerRequest;
import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCheck;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import com.example.meditation.wellness.repository.WellnessAnswerRepository;
import com.example.meditation.wellness.repository.WellnessCheckRepository;
import com.example.meditation.wellness.repository.WellnessQuestionRepository;
import com.example.meditation.wellness.repository.WellnessSurveyRepository;
import com.example.meditation.wellness.service.WellnessScoringService;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
@Order(20)
@ConditionalOnProperty(prefix = "app.wellness.demo", name = "enabled", havingValue = "true")
public class WellnessDemoDataInitializer implements ApplicationRunner {

    private static final int DEMO_MEMBER_COUNT = 5;
    private static final String DEMO_PASSWORD = "Guest2026!";

    private final WellnessSurveyRepository surveyRepository;
    private final WellnessQuestionRepository questionRepository;
    private final WellnessCheckRepository checkRepository;
    private final WellnessAnswerRepository answerRepository;
    private final WellnessScoringService scoringService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public WellnessDemoDataInitializer(
            WellnessSurveyRepository surveyRepository,
            WellnessQuestionRepository questionRepository,
            WellnessCheckRepository checkRepository,
            WellnessAnswerRepository answerRepository,
            WellnessScoringService scoringService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.checkRepository = checkRepository;
        this.answerRepository = answerRepository;
        this.scoringService = scoringService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (checkRepository.count() > 0) {
            return;
        }

        WellnessSurvey survey = surveyRepository
                .findFirstByStatusOrderByVersionDesc(SurveyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("활성화된 웰니스 설문이 없습니다."));
        List<WellnessQuestion> questions = questionRepository
                .findAllBySurveyAndStatusOrderByDisplayOrderAsc(survey, QuestionStatus.ACTIVE);

        for (int memberIndex = 0; memberIndex < DEMO_MEMBER_COUNT; memberIndex++) {
            User user = demoUser(memberIndex);
            createCheck(user, survey, questions, StayStage.BEFORE_STAY, 3, 14 - memberIndex * 2L);
            createCheck(user, survey, questions, StayStage.DURING_STAY, 2, 10 - memberIndex * 2L);
            createCheck(user, survey, questions, StayStage.AFTER_STAY, 1, 6 - memberIndex);
        }
    }

    private User demoUser(int index) {
        String email = "wellness" + (index + 1) + "@msds.local";
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(DEMO_PASSWORD))
                        .name("웰니스 체험객 " + (index + 1))
                        .phoneNumber(String.format("010-7000-%04d", index + 1))
                        .role("USER")
                        .build()));
    }

    private void createCheck(
            User user,
            WellnessSurvey survey,
            List<WellnessQuestion> questions,
            StayStage stage,
            int baseAnswer,
            long daysAgo
    ) {
        List<WellnessAnswerRequest> requests = IntStream.range(0, questions.size())
                .mapToObj(index -> new WellnessAnswerRequest(
                        questions.get(index).getId(),
                        Math.min(4, baseAnswer + index % 2)
                ))
                .toList();
        WellnessScoringService.ScoringResult scoring = scoringService.score(questions, requests);
        WellnessCheck check = checkRepository.save(new WellnessCheck(
                user.getId(),
                null,
                survey,
                stage,
                scoring.totalScore(),
                scoring.level(),
                LocalDateTime.now().minusDays(daysAgo)
        ));
        answerRepository.saveAll(scoring.answers().stream()
                .map(answer -> new WellnessAnswer(
                        check,
                        answer.question(),
                        answer.answerValue(),
                        answer.convertedValue()
                ))
                .toList());
    }
}

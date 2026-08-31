package com.example.meditation.wellness.service;

import com.example.meditation.wellness.dto.request.WellnessCheckRequest;
import com.example.meditation.wellness.dto.response.WellnessCheckResultResponse;
import com.example.meditation.wellness.dto.response.WellnessHistoryResponse;
import com.example.meditation.wellness.dto.response.WellnessQuestionOptionResponse;
import com.example.meditation.wellness.dto.response.WellnessQuestionResponse;
import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCheck;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import com.example.meditation.wellness.repository.WellnessAnswerRepository;
import com.example.meditation.wellness.repository.WellnessCheckRepository;
import com.example.meditation.wellness.repository.WellnessQuestionRepository;
import com.example.meditation.wellness.repository.WellnessSurveyRepository;
import lombok.RequiredArgsConstructor;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessService {

    private static final List<WellnessQuestionOptionResponse> OPTIONS = List.of(
            new WellnessQuestionOptionResponse(0, "전혀 그렇지 않다"),
            new WellnessQuestionOptionResponse(1, "그렇지 않은 편이다"),
            new WellnessQuestionOptionResponse(2, "보통이다"),
            new WellnessQuestionOptionResponse(3, "그런 편이다"),
            new WellnessQuestionOptionResponse(4, "매우 그렇다")
    );

    private final WellnessSurveyRepository surveyRepository;
    private final WellnessQuestionRepository questionRepository;
    private final WellnessCheckRepository checkRepository;
    private final WellnessAnswerRepository answerRepository;
    private final WellnessScoringService scoringService;
    private final UserRepository userRepository;

    public List<WellnessQuestionResponse> getQuestions() {
        return activeQuestions().stream()
                .map(question -> new WellnessQuestionResponse(
                        question.getId(),
                        question.getCategory(),
                        question.getContent(),
                        question.getDisplayOrder(),
                        OPTIONS
                ))
                .toList();
    }

    public WellnessCheckResultResponse checkAsGuest(WellnessCheckRequest request) {
        WellnessScoringService.ScoringResult result = scoringService.score(activeQuestions(), request.answers());
        return toResultResponse(null, result, false);
    }

    @Transactional
    public WellnessCheckResultResponse checkAsMember(String email, WellnessCheckRequest request) {
        User user = findUser(email);
        WellnessSurvey survey = activeSurvey();
        List<WellnessQuestion> questions = activeQuestions(survey);
        WellnessScoringService.ScoringResult result = scoringService.score(questions, request.answers());

        WellnessCheck check = checkRepository.save(new WellnessCheck(
                user.getId(),
                request.reservationId(),
                survey,
                request.resolvedStayStage(),
                result.totalScore(),
                result.level()
        ));

        List<WellnessAnswer> answers = result.answers().stream()
                .map(answer -> new WellnessAnswer(
                        check,
                        answer.question(),
                        answer.answerValue(),
                        answer.convertedValue()
                ))
                .toList();
        answerRepository.saveAll(answers);

        return toResultResponse(check.getId(), result, true);
    }

    public List<WellnessHistoryResponse> getHistory(String email) {
        User user = findUser(email);
        return checkRepository.findAllByMemberIdOrderByCheckedAtDesc(user.getId()).stream()
                .map(check -> new WellnessHistoryResponse(
                        check.getId(),
                        check.getTotalScore(),
                        check.getResultLevel(),
                        check.getStayStage(),
                        check.getCheckedAt()
                ))
                .toList();
    }

    private WellnessCheckResultResponse toResultResponse(
            Long checkId,
            WellnessScoringService.ScoringResult result,
            boolean saved
    ) {
        return new WellnessCheckResultResponse(
                checkId,
                result.totalScore(),
                result.level(),
                result.level().getLabel(),
                result.level().getMessage(),
                saved
        );
    }

    private List<WellnessQuestion> activeQuestions() {
        return activeQuestions(activeSurvey());
    }

    private List<WellnessQuestion> activeQuestions(WellnessSurvey survey) {
        return questionRepository.findAllBySurveyAndStatusOrderByDisplayOrderAsc(
                survey,
                QuestionStatus.ACTIVE
        );
    }

    private WellnessSurvey activeSurvey() {
        return surveyRepository.findFirstByStatusOrderByVersionDesc(SurveyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "활성화된 마음상태 설문이 없습니다."
                ));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}

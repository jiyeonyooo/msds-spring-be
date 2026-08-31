package com.example.meditation.wellness.service;

import com.example.meditation.wellness.dto.request.WellnessAnswerRequest;
import com.example.meditation.wellness.entity.WellnessLevel;
import com.example.meditation.wellness.entity.WellnessQuestion;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WellnessScoringService {

    private static final int MAX_ANSWER_VALUE = 4;

    public ScoringResult score(List<WellnessQuestion> questions, List<WellnessAnswerRequest> answers) {
        if (questions.isEmpty()) {
            throw new IllegalStateException("활성화된 마음상태 문항이 없습니다.");
        }

        Map<Long, Integer> answerByQuestionId = new HashMap<>();
        for (WellnessAnswerRequest answer : answers) {
            Integer previous = answerByQuestionId.put(answer.questionId(), answer.value());
            if (previous != null) {
                throw new IllegalArgumentException("같은 문항에 대한 답변이 중복되었습니다: " + answer.questionId());
            }
        }

        if (answerByQuestionId.size() != questions.size()) {
            throw new IllegalArgumentException("모든 활성 문항에 답변해야 합니다.");
        }

        List<ScoredAnswer> scoredAnswers = questions.stream()
                .map(question -> toScoredAnswer(question, answerByQuestionId))
                .toList();

        int rawScore = scoredAnswers.stream().mapToInt(ScoredAnswer::convertedValue).sum();
        int maximumRawScore = questions.size() * MAX_ANSWER_VALUE;
        int totalScore = Math.round(rawScore * 100f / maximumRawScore);
        WellnessLevel level = WellnessLevel.fromScore(totalScore);

        return new ScoringResult(totalScore, level, scoredAnswers);
    }

    private ScoredAnswer toScoredAnswer(WellnessQuestion question, Map<Long, Integer> answers) {
        Integer value = answers.get(question.getId());
        if (value == null) {
            throw new IllegalArgumentException("답변하지 않은 문항이 있습니다: " + question.getId());
        }
        if (value < 0 || value > MAX_ANSWER_VALUE) {
            throw new IllegalArgumentException("답변 값은 0부터 4까지여야 합니다.");
        }
        return new ScoredAnswer(question, value, question.convert(value));
    }

    public record ScoringResult(
            int totalScore,
            WellnessLevel level,
            List<ScoredAnswer> answers
    ) {
    }

    public record ScoredAnswer(
            WellnessQuestion question,
            int answerValue,
            int convertedValue
    ) {
    }
}

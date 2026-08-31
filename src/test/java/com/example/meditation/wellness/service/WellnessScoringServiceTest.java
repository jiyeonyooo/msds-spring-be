package com.example.meditation.wellness.service;

import com.example.meditation.wellness.dto.request.WellnessAnswerRequest;
import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessLevel;
import com.example.meditation.wellness.entity.WellnessQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WellnessScoringServiceTest {

    private final WellnessScoringService scoringService = new WellnessScoringService();

    @Test
    void 일반_문항과_역채점_문항을_합산해_백점으로_환산한다() {
        WellnessQuestion normal = question(1L, false);
        WellnessQuestion reversed = question(2L, true);

        WellnessScoringService.ScoringResult result = scoringService.score(
                List.of(normal, reversed),
                List.of(new WellnessAnswerRequest(1L, 4), new WellnessAnswerRequest(2L, 0))
        );

        assertThat(result.totalScore()).isEqualTo(100);
        assertThat(result.level()).isEqualTo(WellnessLevel.VERY_TIRED);
        assertThat(result.answers()).extracting(WellnessScoringService.ScoredAnswer::convertedValue)
                .containsExactly(4, 4);
    }

    @Test
    void 중복된_문항_답변은_거부한다() {
        WellnessQuestion question = question(1L, false);

        assertThatThrownBy(() -> scoringService.score(
                List.of(question),
                List.of(new WellnessAnswerRequest(1L, 1), new WellnessAnswerRequest(1L, 2))
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private WellnessQuestion question(Long id, boolean reverseScored) {
        WellnessQuestion question = new WellnessQuestion(
                null,
                WellnessCategory.STRESS,
                "질문",
                id.intValue(),
                reverseScored,
                QuestionStatus.ACTIVE
        );
        ReflectionTestUtils.setField(question, "id", id);
        return question;
    }
}

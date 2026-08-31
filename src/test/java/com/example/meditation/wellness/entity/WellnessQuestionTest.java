package com.example.meditation.wellness.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WellnessQuestionTest {

    @Test
    void 일반_문항은_응답값을_그대로_사용한다() {
        WellnessQuestion question = new WellnessQuestion(
                null, WellnessCategory.STRESS, "질문", 1, false, QuestionStatus.ACTIVE
        );

        assertThat(question.convert(3)).isEqualTo(3);
    }

    @Test
    void 역채점_문항은_4에서_응답값을_뺀다() {
        WellnessQuestion question = new WellnessQuestion(
                null, WellnessCategory.REST, "질문", 1, true, QuestionStatus.ACTIVE
        );

        assertThat(question.convert(3)).isEqualTo(1);
    }
}

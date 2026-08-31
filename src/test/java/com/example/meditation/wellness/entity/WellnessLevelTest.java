package com.example.meditation.wellness.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WellnessLevelTest {

    @Test
    void 점수_경계값에_맞는_단계를_반환한다() {
        assertThat(WellnessLevel.fromScore(20)).isEqualTo(WellnessLevel.VERY_RELAXED);
        assertThat(WellnessLevel.fromScore(21)).isEqualTo(WellnessLevel.RELAXED);
        assertThat(WellnessLevel.fromScore(40)).isEqualTo(WellnessLevel.RELAXED);
        assertThat(WellnessLevel.fromScore(41)).isEqualTo(WellnessLevel.NORMAL);
        assertThat(WellnessLevel.fromScore(60)).isEqualTo(WellnessLevel.NORMAL);
        assertThat(WellnessLevel.fromScore(61)).isEqualTo(WellnessLevel.TIRED);
        assertThat(WellnessLevel.fromScore(80)).isEqualTo(WellnessLevel.TIRED);
        assertThat(WellnessLevel.fromScore(81)).isEqualTo(WellnessLevel.VERY_TIRED);
    }
}

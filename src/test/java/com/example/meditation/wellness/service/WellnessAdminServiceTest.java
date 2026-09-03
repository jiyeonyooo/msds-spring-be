package com.example.meditation.wellness.service;

import com.example.meditation.wellness.dto.response.AdminWellnessStatisticsResponse;
import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.QuestionStatus;
import com.example.meditation.wellness.entity.SurveyStatus;
import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessCheck;
import com.example.meditation.wellness.entity.WellnessLevel;
import com.example.meditation.wellness.entity.WellnessQuestion;
import com.example.meditation.wellness.entity.WellnessSurvey;
import com.example.meditation.wellness.repository.WellnessAnswerRepository;
import com.example.meditation.wellness.repository.WellnessCheckRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WellnessAdminServiceTest {

    private final WellnessCheckRepository checkRepository = mock(WellnessCheckRepository.class);
    private final WellnessAnswerRepository answerRepository = mock(WellnessAnswerRepository.class);
    private final WellnessAdminService service = new WellnessAdminService(
            checkRepository,
            answerRepository,
            2
    );

    @Test
    void 기간별_검사_요약과_분포를_계산한다() {
        WellnessSurvey survey = new WellnessSurvey("마음상태 체크", 1, SurveyStatus.ACTIVE);
        WellnessCheck before = new WellnessCheck(
                1L, null, survey, StayStage.BEFORE_STAY, 20, WellnessLevel.VERY_RELAXED
        );
        WellnessCheck during = new WellnessCheck(
                1L, null, survey, StayStage.DURING_STAY, 60, WellnessLevel.NORMAL
        );
        WellnessCheck after = new WellnessCheck(
                2L, null, survey, StayStage.AFTER_STAY, 80, WellnessLevel.TIRED
        );
        List<WellnessCheck> checks = List.of(before, during, after);
        when(checkRepository.findAllByCheckedAtGreaterThanEqualAndCheckedAtLessThanOrderByCheckedAtAsc(
                any(), any()
        )).thenReturn(checks);
        WellnessQuestion stress = new WellnessQuestion(
                survey, WellnessCategory.STRESS, "스트레스", 1, false, QuestionStatus.ACTIVE
        );
        when(answerRepository.findAllByWellnessCheckIn(checks)).thenReturn(List.of(
                new WellnessAnswer(before, stress, 1, 1),
                new WellnessAnswer(after, stress, 3, 3)
        ));

        LocalDate today = LocalDate.now();
        AdminWellnessStatisticsResponse result = service.getStatistics(today.minusDays(1), today);

        assertThat(result.totalChecks()).isEqualTo(3);
        assertThat(result.uniqueMembers()).isEqualTo(2);
        assertThat(result.averageScore()).isEqualTo(53.3);
        assertThat(result.afterStayAverageChange()).isEqualTo(60.0);
        assertThat(result.stageAverages())
                .filteredOn(item -> item.stage() == StayStage.DURING_STAY)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.averageScore()).isEqualTo(60.0);
                    assertThat(item.count()).isEqualTo(1);
                });
        assertThat(result.categoryAverages())
                .filteredOn(item -> item.category() == WellnessCategory.STRESS)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.averageScore()).isEqualTo(50.0);
                    assertThat(item.answerCount()).isEqualTo(2);
                });
        assertThat(result.levelDistribution())
                .filteredOn(item -> item.level() == WellnessLevel.NORMAL)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.count()).isEqualTo(1);
                    assertThat(item.percentage()).isEqualTo(33.3);
                });
        assertThat(result.dailyTrend()).hasSize(2);
        assertThat(result.dailyTrend().get(1).count()).isEqualTo(3);
    }

    @Test
    void 최소_집계_인원보다_적으면_통계를_숨긴다() {
        WellnessSurvey survey = new WellnessSurvey("마음상태 체크", 1, SurveyStatus.ACTIVE);
        WellnessCheck check = new WellnessCheck(
                1L, null, survey, StayStage.BEFORE_STAY, 20, WellnessLevel.VERY_RELAXED
        );
        when(checkRepository.findAllByCheckedAtGreaterThanEqualAndCheckedAtLessThanOrderByCheckedAtAsc(
                any(), any()
        )).thenReturn(List.of(check));

        LocalDate today = LocalDate.now();
        AdminWellnessStatisticsResponse result = service.getStatistics(today, today);

        assertThat(result.suppressed()).isTrue();
        assertThat(result.minimumMembers()).isEqualTo(2);
        assertThat(result.totalChecks()).isZero();
        assertThat(result.uniqueMembers()).isZero();
        assertThat(result.stageAverages()).allSatisfy(item -> assertThat(item.count()).isZero());
    }
}

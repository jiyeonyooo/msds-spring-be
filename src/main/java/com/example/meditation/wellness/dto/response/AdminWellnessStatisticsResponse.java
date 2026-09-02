package com.example.meditation.wellness.dto.response;

import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessLevel;

import java.time.LocalDate;
import java.util.List;

public record AdminWellnessStatisticsResponse(
        LocalDate fromDate,
        LocalDate toDate,
        boolean suppressed,
        int minimumMembers,
        long totalChecks,
        long uniqueMembers,
        double averageScore,
        double afterStayAverageChange,
        List<StageAverage> stageAverages,
        List<LevelCount> levelDistribution,
        List<CategoryAverage> categoryAverages,
        List<DailyTrend> dailyTrend
) {
    public record LevelCount(
            WellnessLevel level,
            String label,
            long count,
            double percentage
    ) {
    }

    public record StageAverage(
            StayStage stage,
            double averageScore,
            long count
    ) {
    }

    public record CategoryAverage(
            WellnessCategory category,
            double averageScore,
            long answerCount
    ) {
    }

    public record DailyTrend(
            LocalDate date,
            long count,
            double averageScore
    ) {
    }
}

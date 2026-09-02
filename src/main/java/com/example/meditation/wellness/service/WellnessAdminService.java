package com.example.meditation.wellness.service;

import com.example.meditation.wellness.dto.response.AdminWellnessStatisticsResponse;
import com.example.meditation.wellness.entity.StayStage;
import com.example.meditation.wellness.entity.WellnessAnswer;
import com.example.meditation.wellness.entity.WellnessCategory;
import com.example.meditation.wellness.entity.WellnessCheck;
import com.example.meditation.wellness.entity.WellnessLevel;
import com.example.meditation.wellness.repository.WellnessAnswerRepository;
import com.example.meditation.wellness.repository.WellnessCheckRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WellnessAdminService {

    private final WellnessCheckRepository checkRepository;
    private final WellnessAnswerRepository answerRepository;
    private final int minimumMembers;

    public WellnessAdminService(
            WellnessCheckRepository checkRepository,
            WellnessAnswerRepository answerRepository,
            @Value("${app.wellness.statistics.minimum-members:5}") int minimumMembers
    ) {
        this.checkRepository = checkRepository;
        this.answerRepository = answerRepository;
        this.minimumMembers = Math.max(1, minimumMembers);
    }

    public AdminWellnessStatisticsResponse getStatistics(LocalDate fromDate, LocalDate toDate) {
        List<WellnessCheck> checks = checkRepository
                .findAllByCheckedAtGreaterThanEqualAndCheckedAtLessThanOrderByCheckedAtAsc(
                        fromDate.atStartOfDay(),
                        toDate.plusDays(1).atStartOfDay()
                );

        long total = checks.size();
        long uniqueMembers = checks.stream()
                .map(WellnessCheck::getMemberId)
                .distinct()
                .count();
        if (uniqueMembers > 0 && uniqueMembers < minimumMembers) {
            return suppressedStatistics(fromDate, toDate);
        }
        double averageScore = roundedAverage(checks.stream()
                .map(WellnessCheck::getTotalScore)
                .toList());

        Map<StayStage, List<WellnessCheck>> checksByStage = checks.stream()
                .collect(Collectors.groupingBy(WellnessCheck::getStayStage));
        List<AdminWellnessStatisticsResponse.StageAverage> stageAverages = Arrays.stream(StayStage.values())
                .filter(stage -> stage != StayStage.GENERAL)
                .map(stage -> {
                    List<WellnessCheck> stageChecks = checksByStage.getOrDefault(stage, List.of());
                    return new AdminWellnessStatisticsResponse.StageAverage(
                            stage,
                            roundedAverage(stageChecks.stream().map(WellnessCheck::getTotalScore).toList()),
                            stageChecks.size()
                    );
                })
                .toList();
        double beforeAverage = stageAverage(stageAverages, StayStage.BEFORE_STAY);
        double afterAverage = stageAverage(stageAverages, StayStage.AFTER_STAY);
        double afterStayAverageChange = hasStage(stageAverages, StayStage.BEFORE_STAY)
                && hasStage(stageAverages, StayStage.AFTER_STAY)
                ? roundOneDecimal(afterAverage - beforeAverage)
                : 0.0;

        Map<WellnessLevel, Long> levelCounts = checks.stream()
                .collect(Collectors.groupingBy(WellnessCheck::getResultLevel, Collectors.counting()));
        List<AdminWellnessStatisticsResponse.LevelCount> levelDistribution = Arrays.stream(WellnessLevel.values())
                .map(level -> new AdminWellnessStatisticsResponse.LevelCount(
                        level,
                        level.getLabel(),
                        levelCounts.getOrDefault(level, 0L),
                        percentage(levelCounts.getOrDefault(level, 0L), total)
                ))
                .toList();

        List<WellnessAnswer> answers = checks.isEmpty()
                ? List.of()
                : answerRepository.findAllByWellnessCheckIn(checks);
        Map<WellnessCategory, List<WellnessAnswer>> answersByCategory = answers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getWellnessQuestion().getCategory()));
        List<AdminWellnessStatisticsResponse.CategoryAverage> categoryAverages = Arrays
                .stream(WellnessCategory.values())
                .map(category -> {
                    List<WellnessAnswer> categoryAnswers = answersByCategory.getOrDefault(category, List.of());
                    double score = categoryAnswers.isEmpty()
                            ? 0.0
                            : roundOneDecimal(categoryAnswers.stream()
                            .mapToInt(WellnessAnswer::getConvertedValue)
                            .average()
                            .orElse(0.0) * 25.0);
                    return new AdminWellnessStatisticsResponse.CategoryAverage(
                            category,
                            score,
                            categoryAnswers.size()
                    );
                })
                .toList();

        Map<LocalDate, List<WellnessCheck>> checksByDate = checks.stream()
                .collect(Collectors.groupingBy(
                        check -> check.getCheckedAt().toLocalDate(),
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));
        List<AdminWellnessStatisticsResponse.DailyTrend> dailyTrend = fromDate.datesUntil(toDate.plusDays(1))
                .map(date -> {
                    List<WellnessCheck> dailyChecks = checksByDate.getOrDefault(date, List.of());
                    return new AdminWellnessStatisticsResponse.DailyTrend(
                            date,
                            dailyChecks.size(),
                            roundedAverage(dailyChecks.stream().map(WellnessCheck::getTotalScore).toList())
                    );
                })
                .toList();

        return new AdminWellnessStatisticsResponse(
                fromDate,
                toDate,
                false,
                minimumMembers,
                total,
                uniqueMembers,
                averageScore,
                afterStayAverageChange,
                stageAverages,
                levelDistribution,
                categoryAverages,
                dailyTrend
        );
    }

    private AdminWellnessStatisticsResponse suppressedStatistics(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        List<AdminWellnessStatisticsResponse.StageAverage> stages = Arrays.stream(StayStage.values())
                .filter(stage -> stage != StayStage.GENERAL)
                .map(stage -> new AdminWellnessStatisticsResponse.StageAverage(stage, 0.0, 0))
                .toList();
        List<AdminWellnessStatisticsResponse.LevelCount> levels = Arrays.stream(WellnessLevel.values())
                .map(level -> new AdminWellnessStatisticsResponse.LevelCount(
                        level,
                        level.getLabel(),
                        0,
                        0.0
                ))
                .toList();
        List<AdminWellnessStatisticsResponse.CategoryAverage> categories = Arrays
                .stream(WellnessCategory.values())
                .map(category -> new AdminWellnessStatisticsResponse.CategoryAverage(
                        category,
                        0.0,
                        0
                ))
                .toList();
        List<AdminWellnessStatisticsResponse.DailyTrend> trend = fromDate
                .datesUntil(toDate.plusDays(1))
                .map(date -> new AdminWellnessStatisticsResponse.DailyTrend(date, 0, 0.0))
                .toList();

        return new AdminWellnessStatisticsResponse(
                fromDate,
                toDate,
                true,
                minimumMembers,
                0,
                0,
                0.0,
                0.0,
                stages,
                levels,
                categories,
                trend
        );
    }

    private boolean hasStage(
            List<AdminWellnessStatisticsResponse.StageAverage> stages,
            StayStage stage
    ) {
        return stages.stream().anyMatch(item -> item.stage() == stage && item.count() > 0);
    }

    private double stageAverage(
            List<AdminWellnessStatisticsResponse.StageAverage> stages,
            StayStage stage
    ) {
        return stages.stream()
                .filter(item -> item.stage() == stage)
                .mapToDouble(AdminWellnessStatisticsResponse.StageAverage::averageScore)
                .findFirst()
                .orElse(0.0);
    }

    private double percentage(long count, long total) {
        return total == 0 ? 0.0 : roundOneDecimal(count * 100.0 / total);
    }

    private double roundedAverage(List<Integer> scores) {
        return scores.isEmpty()
                ? 0.0
                : roundOneDecimal(scores.stream().mapToInt(Integer::intValue).average().orElse(0.0));
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

package com.example.meditation.quietness.controller;

import com.example.meditation.quietness.dto.response.GuesthouseQuietnessSummaryResponse;
import com.example.meditation.quietness.dto.response.HourlyQuietnessResponse;
import com.example.meditation.quietness.dto.response.QuietnessHistoryPointResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceRecommendationResponse;
import com.example.meditation.quietness.dto.response.SpaceQuietnessResponse;
import com.example.meditation.quietness.service.QuietnessService;
import global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/quietness")
@RequiredArgsConstructor
public class QuietnessController {

    private final QuietnessService quietnessService;

    @GetMapping("/guesthouses/{guesthouseId}/spaces/{spaceId}")
    public ApiResponse<SpaceQuietnessResponse> getCurrentQuietness(
            @PathVariable Long guesthouseId,
            @PathVariable Long spaceId
    ) {
        return ApiResponse.success(
                "현재 조용함 지수를 조회했습니다.",
                quietnessService.getCurrentQuietness(guesthouseId, spaceId)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/summary")
    public ApiResponse<GuesthouseQuietnessSummaryResponse> getGuesthouseSummary(
            @PathVariable Long guesthouseId
    ) {
        return ApiResponse.success(
                "숙소 종합 조용함 지수를 조회했습니다.",
                quietnessService.getGuesthouseSummary(guesthouseId)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/spaces")
    public ApiResponse<List<SpaceQuietnessResponse>> getSpaces(@PathVariable Long guesthouseId) {
        return ApiResponse.success(
                "공간별 조용함 지수를 조회했습니다.",
                quietnessService.getSpaces(guesthouseId)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/recommendation")
    public ApiResponse<QuietSpaceRecommendationResponse> recommendQuietSpace(
            @PathVariable Long guesthouseId
    ) {
        return ApiResponse.success(
                "가장 조용한 공간을 추천했습니다.",
                quietnessService.recommendQuietSpace(guesthouseId)
        );
    }

    @GetMapping("/spaces/{spaceId}/history")
    public ApiResponse<List<QuietnessHistoryPointResponse>> getHistory(
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ApiResponse.success(
                "공간별 조용함 기록을 조회했습니다.",
                quietnessService.getHistory(spaceId, from, to)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/spaces/{spaceId}/hourly")
    public ApiResponse<List<HourlyQuietnessResponse>> getHourlyStatistics(
            @PathVariable Long guesthouseId,
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ApiResponse.success(
                "시간대별 조용함 통계를 조회했습니다.",
                quietnessService.getHourlyStatistics(guesthouseId, spaceId, from, to)
        );
    }
}

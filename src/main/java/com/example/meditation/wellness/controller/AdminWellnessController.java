package com.example.meditation.wellness.controller;

import com.example.meditation.wellness.dto.response.AdminWellnessStatisticsResponse;
import com.example.meditation.wellness.service.WellnessAdminService;
import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/admin/wellness")
@RequiredArgsConstructor
@Tag(name = "관리자 - 마음 기록", description = "관리자용 마음 상태 검사 통계와 집계 결과를 제공합니다.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminWellnessController {

    private static final long MAX_RANGE_DAYS = 365;

    private final WellnessAdminService wellnessAdminService;

    @GetMapping("/statistics")
    @Operation(summary = "마음상태 통계 조회", description = "기간을 생략하면 최근 30일을 조회하며, 최대 조회 범위는 366일입니다.")
    public ApiResponse<AdminWellnessStatisticsResponse> getStatistics(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "조회 종료일", example = "2026-08-30") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        LocalDate resolvedTo = toDate == null ? LocalDate.now() : toDate;
        LocalDate resolvedFrom = fromDate == null ? resolvedTo.minusDays(29) : fromDate;
        long rangeDays = ChronoUnit.DAYS.between(resolvedFrom, resolvedTo);
        if (rangeDays < 0 || rangeDays > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "조회 기간은 시작일이 종료일보다 늦지 않은 최대 366일이어야 합니다."
            );
        }

        return ApiResponse.success(
                "웰니스 통계를 조회했습니다.",
                wellnessAdminService.getStatistics(resolvedFrom, resolvedTo)
        );
    }
}

package com.example.meditation.wellness.controller;

import com.example.meditation.wellness.dto.response.AdminWellnessStatisticsResponse;
import com.example.meditation.wellness.service.WellnessAdminService;
import global.dto.response.ApiResponse;
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
public class AdminWellnessController {

    private static final long MAX_RANGE_DAYS = 365;

    private final WellnessAdminService wellnessAdminService;

    @GetMapping("/statistics")
    public ApiResponse<AdminWellnessStatisticsResponse> getStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
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

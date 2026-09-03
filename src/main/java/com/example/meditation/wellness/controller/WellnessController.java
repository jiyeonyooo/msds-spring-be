package com.example.meditation.wellness.controller;

import com.example.meditation.wellness.dto.request.WellnessCheckRequest;
import com.example.meditation.wellness.dto.response.WellnessCheckResultResponse;
import com.example.meditation.wellness.dto.response.WellnessCheckDetailResponse;
import com.example.meditation.wellness.dto.response.WellnessHistoryResponse;
import com.example.meditation.wellness.dto.response.WellnessQuestionResponse;
import com.example.meditation.wellness.dto.response.WellnessTrendPointResponse;
import com.example.meditation.wellness.service.WellnessService;
import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wellness")
@RequiredArgsConstructor
@Tag(name = "마음 기록")
public class WellnessController {

    private final WellnessService wellnessService;

    @GetMapping("/questions")
    @Operation(summary = "마음상태 검사 문항 조회")
    public ApiResponse<List<WellnessQuestionResponse>> getQuestions() {
        return ApiResponse.success("마음상태 문항을 조회했습니다.", wellnessService.getQuestions());
    }

    @PostMapping("/guest/checks")
    @Operation(summary = "비회원 마음상태 검사", description = "로그인하지 않고 검사 결과를 계산합니다. 결과는 회원 기록에 저장되지 않습니다.")
    public ApiResponse<WellnessCheckResultResponse> checkAsGuest(
            @Valid @RequestBody WellnessCheckRequest request
    ) {
        return ApiResponse.success("마음상태 검사를 완료했습니다.", wellnessService.checkAsGuest(request));
    }

    @PostMapping("/checks")
    @Operation(summary = "회원 마음상태 검사", description = "검사 결과를 계산하고 현재 회원의 기록으로 저장합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ApiResponse<WellnessCheckResultResponse> checkAsMember(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody WellnessCheckRequest request
    ) {
        return ApiResponse.success(
                "마음상태 검사를 저장했습니다.",
                wellnessService.checkAsMember(authentication.getName(), request)
        );
    }

    @GetMapping("/checks/me")
    @Operation(summary = "내 마음상태 검사 내역 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ApiResponse<List<WellnessHistoryResponse>> getMyHistory(
            @Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(
                "마음상태 검사 내역을 조회했습니다.",
                wellnessService.getHistory(authentication.getName())
        );
    }

    @GetMapping("/checks/me/{checkId}")
    @Operation(summary = "내 마음상태 검사 상세 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ApiResponse<WellnessCheckDetailResponse> getMyCheckDetail(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long checkId
    ) {
        return ApiResponse.success(
                "마음상태 검사 상세 결과를 조회했습니다.",
                wellnessService.getCheckDetail(authentication.getName(), checkId)
        );
    }

    @GetMapping("/trends/me")
    @Operation(summary = "내 마음상태 변화 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ApiResponse<List<WellnessTrendPointResponse>> getMyTrend(
            @Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(
                "마음상태 변화 데이터를 조회했습니다.",
                wellnessService.getTrend(authentication.getName())
        );
    }
}

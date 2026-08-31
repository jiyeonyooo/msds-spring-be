package com.example.meditation.wellness.controller;

import com.example.meditation.wellness.dto.request.WellnessCheckRequest;
import com.example.meditation.wellness.dto.response.WellnessCheckResultResponse;
import com.example.meditation.wellness.dto.response.WellnessCheckDetailResponse;
import com.example.meditation.wellness.dto.response.WellnessHistoryResponse;
import com.example.meditation.wellness.dto.response.WellnessQuestionResponse;
import com.example.meditation.wellness.dto.response.WellnessTrendPointResponse;
import com.example.meditation.wellness.service.WellnessService;
import global.dto.response.ApiResponse;
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
public class WellnessController {

    private final WellnessService wellnessService;

    @GetMapping("/questions")
    public ApiResponse<List<WellnessQuestionResponse>> getQuestions() {
        return ApiResponse.success("마음상태 문항을 조회했습니다.", wellnessService.getQuestions());
    }

    @PostMapping("/guest/checks")
    public ApiResponse<WellnessCheckResultResponse> checkAsGuest(
            @Valid @RequestBody WellnessCheckRequest request
    ) {
        return ApiResponse.success("마음상태 검사를 완료했습니다.", wellnessService.checkAsGuest(request));
    }

    @PostMapping("/checks")
    public ApiResponse<WellnessCheckResultResponse> checkAsMember(
            Authentication authentication,
            @Valid @RequestBody WellnessCheckRequest request
    ) {
        return ApiResponse.success(
                "마음상태 검사를 저장했습니다.",
                wellnessService.checkAsMember(authentication.getName(), request)
        );
    }

    @GetMapping("/checks/me")
    public ApiResponse<List<WellnessHistoryResponse>> getMyHistory(Authentication authentication) {
        return ApiResponse.success(
                "마음상태 검사 내역을 조회했습니다.",
                wellnessService.getHistory(authentication.getName())
        );
    }

    @GetMapping("/checks/me/{checkId}")
    public ApiResponse<WellnessCheckDetailResponse> getMyCheckDetail(
            Authentication authentication,
            @PathVariable Long checkId
    ) {
        return ApiResponse.success(
                "마음상태 검사 상세 결과를 조회했습니다.",
                wellnessService.getCheckDetail(authentication.getName(), checkId)
        );
    }

    @GetMapping("/trends/me")
    public ApiResponse<List<WellnessTrendPointResponse>> getMyTrend(Authentication authentication) {
        return ApiResponse.success(
                "마음상태 변화 데이터를 조회했습니다.",
                wellnessService.getTrend(authentication.getName())
        );
    }
}

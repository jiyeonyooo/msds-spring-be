package com.example.meditation.quietness.controller;

import com.example.meditation.quietness.dto.request.NoiseDeviceCreateRequest;
import com.example.meditation.quietness.dto.request.NoiseDeviceStatusUpdateRequest;
import com.example.meditation.quietness.dto.request.NoiseMeasurementCreateRequest;
import com.example.meditation.quietness.dto.request.QuietSpaceCreateRequest;
import com.example.meditation.quietness.dto.request.QuietnessThresholdUpdateRequest;
import com.example.meditation.quietness.dto.response.NoiseDeviceResponse;
import com.example.meditation.quietness.dto.response.NoiseMeasurementResponse;
import com.example.meditation.quietness.dto.response.QuietSpaceResponse;
import com.example.meditation.quietness.dto.response.QuietnessThresholdResponse;
import com.example.meditation.quietness.service.QuietnessAdminService;
import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quietness")
@RequiredArgsConstructor
@Tag(name = "관리자 - 조용함")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminQuietnessController {

    private final QuietnessAdminService quietnessAdminService;

    @GetMapping("/guesthouses/{guesthouseId}/spaces")
    @Operation(summary = "조용함 측정 공간 목록 조회")
    public ApiResponse<List<QuietSpaceResponse>> getSpaces(@PathVariable Long guesthouseId) {
        return ApiResponse.success(
                "조용함 측정 공간 목록을 조회했습니다.",
                quietnessAdminService.getSpaces(guesthouseId)
        );
    }

    @PostMapping("/spaces")
    @Operation(summary = "조용함 측정 공간 등록")
    public ApiResponse<QuietSpaceResponse> createSpace(
            @Valid @RequestBody QuietSpaceCreateRequest request
    ) {
        return ApiResponse.success(
                "조용함 측정 공간을 등록했습니다.",
                quietnessAdminService.createSpace(request)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/devices")
    @Operation(summary = "소음 측정 기기 목록 조회")
    public ApiResponse<List<NoiseDeviceResponse>> getDevices(@PathVariable Long guesthouseId) {
        return ApiResponse.success(
                "소음 측정기기 목록을 조회했습니다.",
                quietnessAdminService.getDevices(guesthouseId)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/thresholds")
    @Operation(summary = "조용함 기준값 조회")
    public ApiResponse<List<QuietnessThresholdResponse>> getThresholds(
            @PathVariable Long guesthouseId
    ) {
        return ApiResponse.success(
                "조용함 기준값을 조회했습니다.",
                quietnessAdminService.getThresholds(guesthouseId)
        );
    }

    @PatchMapping("/guesthouses/{guesthouseId}/thresholds")
    @Operation(summary = "조용함 기준값 변경")
    public ApiResponse<List<QuietnessThresholdResponse>> updateThresholds(
            @PathVariable Long guesthouseId,
            @Valid @RequestBody QuietnessThresholdUpdateRequest request
    ) {
        return ApiResponse.success(
                "조용함 기준값을 변경했습니다.",
                quietnessAdminService.updateThresholds(guesthouseId, request)
        );
    }

    @PostMapping("/devices")
    @Operation(summary = "소음 측정 기기 등록")
    public ApiResponse<NoiseDeviceResponse> createDevice(
            @Valid @RequestBody NoiseDeviceCreateRequest request
    ) {
        return ApiResponse.success(
                "소음 측정기기를 등록했습니다.",
                quietnessAdminService.createDevice(request)
        );
    }

    @PatchMapping("/devices/{deviceId}/status")
    @Operation(summary = "소음 측정 기기 상태 변경")
    public ApiResponse<NoiseDeviceResponse> updateDeviceStatus(
            @PathVariable Long deviceId,
            @Valid @RequestBody NoiseDeviceStatusUpdateRequest request
    ) {
        return ApiResponse.success(
                "소음 측정기기 상태를 변경했습니다.",
                quietnessAdminService.updateDeviceStatus(deviceId, request)
        );
    }

    @PostMapping("/measurements")
    @Operation(summary = "소음 측정값 등록", description = "측정 기기가 수집한 데시벨 값을 저장합니다.")
    public ApiResponse<NoiseMeasurementResponse> createMeasurement(
            @Valid @RequestBody NoiseMeasurementCreateRequest request
    ) {
        return ApiResponse.success(
                "소음 측정값을 등록했습니다.",
                quietnessAdminService.createMeasurement(request)
        );
    }
}

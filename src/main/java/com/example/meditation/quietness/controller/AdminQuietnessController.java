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
public class AdminQuietnessController {

    private final QuietnessAdminService quietnessAdminService;

    @GetMapping("/guesthouses/{guesthouseId}/spaces")
    public ApiResponse<List<QuietSpaceResponse>> getSpaces(@PathVariable Long guesthouseId) {
        return ApiResponse.success(
                "조용함 측정 공간 목록을 조회했습니다.",
                quietnessAdminService.getSpaces(guesthouseId)
        );
    }

    @PostMapping("/spaces")
    public ApiResponse<QuietSpaceResponse> createSpace(
            @Valid @RequestBody QuietSpaceCreateRequest request
    ) {
        return ApiResponse.success(
                "조용함 측정 공간을 등록했습니다.",
                quietnessAdminService.createSpace(request)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/devices")
    public ApiResponse<List<NoiseDeviceResponse>> getDevices(@PathVariable Long guesthouseId) {
        return ApiResponse.success(
                "소음 측정기기 목록을 조회했습니다.",
                quietnessAdminService.getDevices(guesthouseId)
        );
    }

    @GetMapping("/guesthouses/{guesthouseId}/thresholds")
    public ApiResponse<List<QuietnessThresholdResponse>> getThresholds(
            @PathVariable Long guesthouseId
    ) {
        return ApiResponse.success(
                "조용함 기준값을 조회했습니다.",
                quietnessAdminService.getThresholds(guesthouseId)
        );
    }

    @PatchMapping("/guesthouses/{guesthouseId}/thresholds")
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
    public ApiResponse<NoiseDeviceResponse> createDevice(
            @Valid @RequestBody NoiseDeviceCreateRequest request
    ) {
        return ApiResponse.success(
                "소음 측정기기를 등록했습니다.",
                quietnessAdminService.createDevice(request)
        );
    }

    @PatchMapping("/devices/{deviceId}/status")
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
    public ApiResponse<NoiseMeasurementResponse> createMeasurement(
            @Valid @RequestBody NoiseMeasurementCreateRequest request
    ) {
        return ApiResponse.success(
                "소음 측정값을 등록했습니다.",
                quietnessAdminService.createMeasurement(request)
        );
    }
}

package room.controller;

import global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import room.dto.response.FacilitySummaryResponse;
import room.entity.enums.FacilityCategory;
import room.service.FacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
@Tag(name = "편의시설", description = "고객에게 공개되는 편의시설 목록과 상세 정보를 조회합니다.")
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    @Operation(summary = "편의시설 목록 조회", description = "카테고리를 생략하면 전체 편의시설을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FacilitySummaryResponse>>> getFacilities(
            @Parameter(description = "편의시설 카테고리") @RequestParam(required = false) FacilityCategory category
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "편의시설 목록 조회에 성공했습니다.",
                facilityService.getFacilities(category)
        );
    }
}

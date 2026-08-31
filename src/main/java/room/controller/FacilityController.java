package room.controller;

import global.dto.response.ApiResponse;
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
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacilitySummaryResponse>>> getFacilities(
            @RequestParam(required = false) FacilityCategory category
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "편의시설 목록 조회에 성공했습니다.",
                facilityService.getFacilities(category)
        );
    }
}

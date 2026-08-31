package resv.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import global.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import resv.dto.CreateResvRequestDTO;
import resv.dto.MyResvSearchRequestDTO;
import resv.dto.ResvAvailabilityRequestDTO;

@RestController
@RequestMapping("/api/resv")
@Validated
public class UserResvController {
    //GET api/resv: 날짜/인원 기준 예약 현황 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getResv(@Valid @ModelAttribute ResvAvailabilityRequestDTO request) {
        System.out.println("GET api/resv: 전체 예약현황 조회 API 실행");
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", null));
    }

    //POST api/resv: 예약 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createResv(
            @Valid @RequestBody CreateResvRequestDTO crr){
        System.out.println("POST api/resv: 예약 생성 API 실행");
        System.out.println(crr);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("예약이 성공적으로 생성되었습니다.", null));
    }

    //GET api/resv/me: 내 예약 목록 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Void>> getResvMe(@Valid @ModelAttribute MyResvSearchRequestDTO request) {
        System.out.println("GET api/resv/me: 내 예약 목록 조회 API 실행");
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", null));
    }

    //GET api/resv/{resvId}: 예약 상세 조회
    @GetMapping("/{resvId}")
    public ResponseEntity<ApiResponse<Void>> getResvDetail(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        System.out.println("GET api/resv/{resvId}: 예약 상세 조회 API 실행");
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", null));
    }

    //PATCH api/resv/{resvId}/cancel: 예약 취소
    @PatchMapping("/{resvId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelResv(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        System.out.println("PATCH api/resv/{resvId}/cancel: 예약 취소 API 실행");
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.", null));
    }
}

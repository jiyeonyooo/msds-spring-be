package resv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resv.enums.ResvStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/resv")
public class AdminResvController {
    //GET api/admin/resv: 관리자 전체 예약 조회/검색/필터
    @GetMapping
    public ResponseEntity getAdminResv(
            @RequestParam(required = false) ResvStatus resvStatus,
            @RequestParam(required = false) LocalDate searchFromDate,
            @RequestParam(required = false) LocalDate searchToDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        System.out.println("GET api/admin/resv: 관리자 전체 예약 조회/검색/필터 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //GET api/admin/resv/{resvId}: 관리자 예약 상세 조회
    @GetMapping("/{resvId}")
    public ResponseEntity getAdminResvDetail(
            @PathVariable long resvId) {
        System.out.println("GET api/admin/resv/{resvId}: 관리자 예약 상세 조회 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //PATCH api/admin/resv/{resvId}/status: 관리자 예약 상태 변경
    @PatchMapping("/{resvId}")
    public ResponseEntity patchAdminResv(
            @PathVariable long resvId) {
        System.out.println("PATCH api/admin/resv/{resvId}/status: 관리자 예약 상태 변경 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}

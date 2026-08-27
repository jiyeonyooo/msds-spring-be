package resv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resv.dto.CreateResvRequestDTO;
import resv.enums.ResvStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/resv")
public class UserResvController {
    //GET api/resv: 날짜/인원 기준 예약 현황 조회
    @GetMapping
    public ResponseEntity getResv(
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate,
            @RequestParam int guestCount) {
        System.out.println("GET api/resv: 전체 예약현황 조회 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //POST api/resv: 예약 생성
    @PostMapping
    public ResponseEntity createResv(
            @RequestBody CreateResvRequestDTO crr){
        System.out.println("POST api/resv: 예약 생성 API 실행");
        System.out.println(crr);
        return ResponseEntity.status(HttpStatus.CREATED).body("");
    }

    //GET api/resv/me: 내 예약 목록 조회
    @GetMapping("/me")
    public ResponseEntity getResvMe(
            @RequestParam(required = false) ResvStatus resvStatus,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        System.out.println("GET api/resv/me: 내 예약 목록 조회 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //GET api/resv/{resvId}: 예약 상세 조회
    @GetMapping("/{resvId}")
    public ResponseEntity getResvDetail(
            @PathVariable long resvId) {
        System.out.println("GET api/resv/{resvId}: 예약 상세 조회 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //PATCH api/resv/{resvId}/cancel: 예약 취소
    @PatchMapping("/{resvId}/cancel")
    public ResponseEntity cancelResv(
            @PathVariable long resvId) {
        System.out.println("PATCH api/resv/{resvId}/cancel: 예약 취소 API 실행");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}

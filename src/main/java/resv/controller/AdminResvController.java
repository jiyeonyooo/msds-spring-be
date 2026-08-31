package resv.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import global.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import resv.dto.AdminResvSearchRequestDTO;

@RestController
@RequestMapping("/api/admin/resv")
@Validated
public class AdminResvController {
    //GET api/admin/resv: ??? ?? ?? ??/??/??
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getAdminResv(@Valid @ModelAttribute AdminResvSearchRequestDTO request) {
        System.out.println("GET api/admin/resv: ??? ?? ?? ??/??/?? API ??");
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", null));
    }

    //GET api/admin/resv/{resvId}: ??? ?? ?? ??
    @GetMapping("/{resvId}")
    public ResponseEntity<ApiResponse<Void>> getAdminResvDetail(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        System.out.println("GET api/admin/resv/{resvId}: ??? ?? ?? ?? API ??");
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다.", null));
    }

    //PATCH api/admin/resv/{resvId}/status: ??? ?? ?? ??
    @PatchMapping("/{resvId}/status")
    public ResponseEntity<ApiResponse<Void>> patchAdminResv(
            @PathVariable @Positive(message = "예약 ID는 1 이상의 값이어야 합니다.") long resvId) {
        System.out.println("PATCH api/admin/resv/{resvId}/status: ??? ?? ?? ?? API ??");
        return ResponseEntity.ok(ApiResponse.success("예약이 성공적으로 취소되었습니다.", null));
    }
}

package resv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resv.enums.ResvStatus;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/resv")
public class AdminResvController {
    //GET api/admin/resv: ??? ?? ?? ??/??/??
    @GetMapping
    public ResponseEntity getAdminResv(
            @RequestParam(required = false) ResvStatus resvStatus,
            @RequestParam(required = false) LocalDate searchFromDate,
            @RequestParam(required = false) LocalDate searchToDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        System.out.println("GET api/admin/resv: ??? ?? ?? ??/??/?? API ??");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //GET api/admin/resv/{resvId}: ??? ?? ?? ??
    @GetMapping("/{resvId}")
    public ResponseEntity getAdminResvDetail(
            @PathVariable long resvId) {
        System.out.println("GET api/admin/resv/{resvId}: ??? ?? ?? ?? API ??");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    //PATCH api/admin/resv/{resvId}/status: ??? ?? ?? ??
    @PatchMapping("/{resvId}/status")
    public ResponseEntity patchAdminResv(
            @PathVariable long resvId) {
        System.out.println("PATCH api/admin/resv/{resvId}/status: ??? ?? ?? ?? API ??");
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}

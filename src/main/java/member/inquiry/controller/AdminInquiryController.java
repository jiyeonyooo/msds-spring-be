package member.inquiry.controller;

import global.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import member.inquiry.domain.InquiryStatus;
import member.inquiry.dto.InquiryAnswerRequest;
import member.inquiry.dto.InquiryResponse;
import member.inquiry.service.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 문의 API.
 * 전체 문의 조회 및 답변 등록을 담당한다.
 * 접근 제어는 SecurityConfig의 /api/admin/** hasRole("ADMIN")에서 1차로 처리되고,
 * 서비스 계층에서 User.role을 한 번 더 확인한다(이중 방어).
 */
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    /**
     * 1. 전체 문의 목록 조회 - GET /api/admin/inquiries
     * status 쿼리 파라미터(WAITING/ANSWERED)로 미답변 문의만 걸러볼 수 있다.
     * 예) GET /api/admin/inquiries?status=WAITING
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getAllInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) InquiryStatus status) {

        String adminEmail = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getAllInquiriesForAdmin(adminEmail, status);
        return ResponseEntity.ok(ApiResponse.success("문의 목록 조회에 성공했습니다.", responses));
    }

    // 2. 문의 상세 조회 - GET /api/admin/inquiries/{inquiryId}
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId) {

        String adminEmail = userDetails.getUsername();
        InquiryResponse response = inquiryService.getInquiryDetailForAdmin(adminEmail, inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 조회에 성공했습니다.", response));
    }

    // 3. 문의 답변 등록 - PATCH /api/admin/inquiries/{inquiryId}/answer
    @PatchMapping("/{inquiryId}/answer")
    public ResponseEntity<ApiResponse<InquiryResponse>> answerInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequest request) {

        String adminEmail = userDetails.getUsername();
        InquiryResponse response = inquiryService.answerInquiry(adminEmail, inquiryId, request);
        return ResponseEntity.ok(ApiResponse.success("답변이 등록되었습니다.", response));
    }
}

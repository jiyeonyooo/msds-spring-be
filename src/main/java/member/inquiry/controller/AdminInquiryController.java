package member.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * 권한 검증은 현재 InquiryServiceImpl에서 User.role 컬럼을 직접 확인하는 방식으로 처리 중.
 * (SecurityConfig에서 /api/admin/** 경로 자체를 ROLE_ADMIN으로 제한하도록 정비되면
 *  이 컨트롤러 레벨의 이중 방어까지는 필수는 아니지만 남겨둬도 무방)
 */
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    // 1. 전체 문의 목록 조회 - GET /api/admin/inquiries
    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getAllInquiries(
            @AuthenticationPrincipal UserDetails userDetails) {

        String adminEmail = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getAllInquiriesForAdmin(adminEmail);
        return ResponseEntity.ok(responses);
    }

    // 2. 문의 답변 등록 - PATCH /api/admin/inquiries/{inquiryId}/answer
    @PatchMapping("/{inquiryId}/answer")
    public ResponseEntity<InquiryResponse> answerInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequest request) {

        String adminEmail = userDetails.getUsername();
        InquiryResponse response = inquiryService.answerInquiry(adminEmail, inquiryId, request);
        return ResponseEntity.ok(response);
    }
}
package member.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import member.inquiry.dto.InquiryCreateRequest;
import member.inquiry.dto.InquiryResponse;
import member.inquiry.service.InquiryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 일반 회원용 문의 API.
 * 본인이 작성한 문의의 작성/조회만 담당한다.
 */
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 1. 문의 작성 - POST /api/inquiries
    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequest request) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.createInquiry(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 내 문의 목록 조회 - GET /api/inquiries
    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getMyInquiries(email);
        return ResponseEntity.ok(responses);
    }

    // 3. 내 문의 상세 조회 - GET /api/inquiries/{inquiryId}
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponse> getMyInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.getMyInquiryDetail(email, inquiryId);
        return ResponseEntity.ok(response);
    }
}
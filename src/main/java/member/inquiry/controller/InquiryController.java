package member.inquiry.controller;

import global.dto.response.ApiResponse;
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
 * 응답은 공통 규격인 ApiResponse(code, message, data)로 감싸서 내려준다.
 */
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 1. 문의 작성 - POST /api/inquiries
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequest request) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.createInquiry(email, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("문의가 등록되었습니다.", response));
    }

    // 2. 내 문의 목록 조회 - GET /api/inquiries
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getMyInquiries(email);
        return ResponseEntity.ok(ApiResponse.success("내 문의 목록 조회에 성공했습니다.", responses));
    }

    // 3. 내 문의 상세 조회 - GET /api/inquiries/{inquiryId}
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<InquiryResponse>> getMyInquiryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.getMyInquiryDetail(email, inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 조회에 성공했습니다.", response));
    }
}

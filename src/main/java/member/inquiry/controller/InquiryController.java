package member.inquiry.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "문의", description = "로그인 회원의 1:1 문의 등록과 조회를 처리합니다.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class InquiryController {

    private final InquiryService inquiryService;

    // 1. 문의 작성 - POST /api/inquiries
    @PostMapping
    @Operation(summary = "문의 작성")
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequest request) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.createInquiry(email, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("문의가 등록되었습니다.", response));
    }

    // 2. 내 문의 목록 조회 - GET /api/inquiries
    @GetMapping
    @Operation(summary = "내 문의 목록 조회")
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getMyInquiries(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getMyInquiries(email);
        return ResponseEntity.ok(ApiResponse.success("내 문의 목록 조회에 성공했습니다.", responses));
    }

    // 3. 내 문의 상세 조회 - GET /api/inquiries/{inquiryId}
    @GetMapping("/{inquiryId}")
    @Operation(summary = "내 문의 상세 조회", description = "현재 회원이 작성한 문의만 조회할 수 있습니다.")
    public ResponseEntity<ApiResponse<InquiryResponse>> getMyInquiryDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId) {

        String email = userDetails.getUsername();
        InquiryResponse response = inquiryService.getMyInquiryDetail(email, inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 조회에 성공했습니다.", response));
    }
}

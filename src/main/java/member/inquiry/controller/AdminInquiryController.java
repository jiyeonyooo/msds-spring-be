package member.inquiry.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "관리자 - 문의", description = "회원 문의를 조회하고 답변 및 처리 상태를 관리합니다.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminInquiryController {

    private final InquiryService inquiryService;

    /**
     * 1. 전체 문의 목록 조회 - GET /api/admin/inquiries
     * status 쿼리 파라미터(WAITING/ANSWERED)로 미답변 문의만 걸러볼 수 있다.
     * 예) GET /api/admin/inquiries?status=WAITING
     */
    @GetMapping
    @Operation(summary = "전체 문의 목록 조회", description = "답변 상태로 문의를 필터링할 수 있습니다.")
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getAllInquiries(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "답변 상태") @RequestParam(required = false) InquiryStatus status) {

        String adminEmail = userDetails.getUsername();
        List<InquiryResponse> responses = inquiryService.getAllInquiriesForAdmin(adminEmail, status);
        return ResponseEntity.ok(ApiResponse.success("문의 목록 조회에 성공했습니다.", responses));
    }

    // 2. 문의 상세 조회 - GET /api/admin/inquiries/{inquiryId}
    @GetMapping("/{inquiryId}")
    @Operation(summary = "문의 상세 조회")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId) {

        String adminEmail = userDetails.getUsername();
        InquiryResponse response = inquiryService.getInquiryDetailForAdmin(adminEmail, inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의 상세 조회에 성공했습니다.", response));
    }

    // 3. 문의 답변 등록 - PATCH /api/admin/inquiries/{inquiryId}/answer
    @PatchMapping("/{inquiryId}/answer")
    @Operation(summary = "문의 답변 등록", description = "문의에 관리자 답변을 등록하고 상태를 답변 완료로 변경합니다.")
    public ResponseEntity<ApiResponse<InquiryResponse>> answerInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequest request) {

        String adminEmail = userDetails.getUsername();
        InquiryResponse response = inquiryService.answerInquiry(adminEmail, inquiryId, request);
        return ResponseEntity.ok(ApiResponse.success("답변이 등록되었습니다.", response));
    }
}

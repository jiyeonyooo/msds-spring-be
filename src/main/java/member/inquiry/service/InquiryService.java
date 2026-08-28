package member.inquiry.service;

import member.inquiry.dto.InquiryAnswerRequest;
import member.inquiry.dto.InquiryCreateRequest;
import member.inquiry.dto.InquiryResponse;

import java.util.List;

public interface InquiryService {

    // 문의 작성 (일반 회원)
    InquiryResponse createInquiry(String email, InquiryCreateRequest request);

    // 내 문의 목록 조회 (일반 회원)
    List<InquiryResponse> getMyInquiries(String email);

    // 내 문의 상세 조회 (일반 회원, 본인 소유 문의만)
    InquiryResponse getMyInquiryDetail(String email, Long inquiryId);

    // 전체 문의 목록 조회 (관리자 전용)
    List<InquiryResponse> getAllInquiriesForAdmin(String adminEmail);

    // 문의 답변 등록 (관리자 전용)
    InquiryResponse answerInquiry(String adminEmail, Long inquiryId, InquiryAnswerRequest request);
}
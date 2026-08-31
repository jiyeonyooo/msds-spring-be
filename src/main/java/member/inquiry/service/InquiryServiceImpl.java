package member.inquiry.service;

import lombok.RequiredArgsConstructor;
import member.common.exception.MemberErrorCode;
import member.common.exception.MemberException;
import member.inquiry.domain.Inquiry;
import member.inquiry.domain.InquiryStatus;
import member.inquiry.dto.InquiryAnswerRequest;
import member.inquiry.dto.InquiryCreateRequest;
import member.inquiry.dto.InquiryResponse;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * InquiryService 구현체.
 * 관리자 권한은 SecurityConfig의 /api/admin/** hasRole("ADMIN")에서 1차로 걸러지지만,
 * 서비스 단독 호출(테스트/내부 호출)에서도 안전하도록 User.role을 한 번 더 검증한다(이중 방어).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(String email, InquiryCreateRequest request) {
        User user = findUserByEmail(email);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        inquiryRepository.save(inquiry);
        return InquiryResponse.from(inquiry);
    }

    @Override
    public List<InquiryResponse> getMyInquiries(String email) {
        User user = findUserByEmail(email);

        // join fetch 조회라 목록 매핑 중 작성자 정보를 다시 조회하지 않는다(N+1 방지)
        return toResponses(inquiryRepository.findAllByUserIdWithUser(user.getId()));
    }

    @Override
    public InquiryResponse getMyInquiryDetail(String email, Long inquiryId) {
        User user = findUserByEmail(email);
        Inquiry inquiry = findInquiryById(inquiryId);

        // 본인이 작성한 문의가 아니면 접근 차단 (403)
        if (!inquiry.isOwnedBy(user.getId())) {
            throw new MemberException(MemberErrorCode.INQUIRY_FORBIDDEN);
        }

        return InquiryResponse.from(inquiry);
    }

    @Override
    public List<InquiryResponse> getAllInquiriesForAdmin(String adminEmail, InquiryStatus status) {
        validateAdmin(adminEmail);

        // status 파라미터가 없으면 전체, 있으면 해당 상태(WAITING/ANSWERED)만 조회
        List<Inquiry> inquiries = (status == null)
                ? inquiryRepository.findAllWithUser()
                : inquiryRepository.findAllByStatusWithUser(status);

        return toResponses(inquiries);
    }

    @Override
    @Transactional
    public InquiryResponse answerInquiry(String adminEmail, Long inquiryId, InquiryAnswerRequest request) {
        validateAdmin(adminEmail);

        Inquiry inquiry = findInquiryById(inquiryId);
        inquiry.answer(request.getAnswerContent());
        // 변경 감지(Dirty Checking)로 트랜잭션 커밋 시 UPDATE 실행, updatedAt은 @PreUpdate로 자동 갱신

        return InquiryResponse.from(inquiry);
    }

    private List<InquiryResponse> toResponses(List<Inquiry> inquiries) {
        return inquiries.stream()
                .map(InquiryResponse::from)
                .toList();
    }

    private User findUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }

    private Inquiry findInquiryById(Long inquiryId) {
        return inquiryRepository.findByIdWithUser(inquiryId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.INQUIRY_NOT_FOUND));
    }

    private void validateAdmin(String email) {
        User user = findUserByEmail(email);
        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new MemberException(MemberErrorCode.ADMIN_ONLY);
        }
    }

    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}

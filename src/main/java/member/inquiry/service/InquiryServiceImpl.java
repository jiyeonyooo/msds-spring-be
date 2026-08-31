package member.inquiry.service;

import lombok.RequiredArgsConstructor;
import member.inquiry.domain.Inquiry;
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
 * 관리자 권한 체크는 Spring Security의 authorities 설정이 아직 없는 상태를 감안해
 * User 엔티티의 role 컬럼을 직접 조회해서 검증한다.
 * (추후 SecurityConfig/JWT 필터에서 ROLE_ADMIN 권한 부여가 정비되면
 * 컨트롤러 레벨 @PreAuthorize("hasRole('ADMIN')")로 옮기는 걸 권장.)
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

        return inquiryRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(InquiryResponse::from)
                .toList();
    }

    @Override
    public InquiryResponse getMyInquiryDetail(String email, Long inquiryId) {
        User user = findUserByEmail(email);
        Inquiry inquiry = findInquiryById(inquiryId);

        // 본인이 작성한 문의가 아니면 접근 차단
        if (!inquiry.isOwnedBy(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 문의만 조회할 수 있습니다.");
        }

        return InquiryResponse.from(inquiry);
    }

    @Override
    public List<InquiryResponse> getAllInquiriesForAdmin(String adminEmail) {
        validateAdmin(adminEmail);

        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(InquiryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public InquiryResponse answerInquiry(String adminEmail, Long inquiryId, InquiryAnswerRequest request) {
        validateAdmin(adminEmail);

        Inquiry inquiry = findInquiryById(inquiryId);
        inquiry.answer(request.getAnswerContent());
        // updatedAt은 @PreUpdate로 자동 갱신됨

        return InquiryResponse.from(inquiry);
    }

    private User findUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private Inquiry findInquiryById(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));
    }

    private void validateAdmin(String email) {
        User user = findUserByEmail(email);
        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
    }

    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}
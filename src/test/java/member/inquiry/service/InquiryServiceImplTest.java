package member.inquiry.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private User buildUser(long id, String email, String role) {
        User user = User.builder()
                .email(email)
                .password("encoded")
                .name("사용자")
                .phoneNumber("010-1234-5678")
                .role(role)
                .build();
        setId(user, id);
        return user;
    }

    private Inquiry buildInquiry(long id, User user) {
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .build();
        setId(inquiry, id);
        return inquiry;
    }

    @Test
    @DisplayName("문의를 작성하면 WAITING 상태로 저장된다")
    void createInquiry_success() {
        User user = buildUser(1L, "user@example.com", "USER");
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

        InquiryCreateRequest request = new InquiryCreateRequest();
        setField(request, "title", "문의 제목");
        setField(request, "content", "문의 내용");

        InquiryResponse response = inquiryService.createInquiry("user@example.com", request);

        assertThat(response.getStatus()).isEqualTo(InquiryStatus.WAITING);
        assertThat(response.getAuthorEmail()).isEqualTo("user@example.com");
        verify(inquiryRepository).save(org.mockito.ArgumentMatchers.any(Inquiry.class));
    }

    @Test
    @DisplayName("본인이 작성한 문의는 정상 조회된다")
    void getMyInquiryDetail_success_owner() {
        User owner = buildUser(1L, "owner@example.com", "USER");
        Inquiry inquiry = buildInquiry(10L, owner);

        given(userRepository.findByEmail("owner@example.com")).willReturn(Optional.of(owner));
        given(inquiryRepository.findByIdWithUser(10L)).willReturn(Optional.of(inquiry));

        InquiryResponse response = inquiryService.getMyInquiryDetail("owner@example.com", 10L);

        assertThat(response.getInquiryId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("다른 사람이 작성한 문의를 조회하면 예외가 발생한다")
    void getMyInquiryDetail_fail_notOwner() {
        User owner = buildUser(1L, "owner@example.com", "USER");
        User stranger = buildUser(2L, "stranger@example.com", "USER");
        Inquiry inquiry = buildInquiry(10L, owner);

        given(userRepository.findByEmail("stranger@example.com")).willReturn(Optional.of(stranger));
        given(inquiryRepository.findByIdWithUser(10L)).willReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> inquiryService.getMyInquiryDetail("stranger@example.com", 10L))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.INQUIRY_FORBIDDEN);
    }

    @Test
    @DisplayName("일반 회원(USER)은 전체 문의 목록을 조회할 수 없다")
    void getAllInquiriesForAdmin_fail_notAdmin() {
        User normalUser = buildUser(1L, "user@example.com", "USER");
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(normalUser));

        assertThatThrownBy(() -> inquiryService.getAllInquiriesForAdmin("user@example.com", null))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.ADMIN_ONLY);
    }

    @Test
    @DisplayName("관리자는 전체 문의 목록을 조회할 수 있다")
    void getAllInquiriesForAdmin_success() {
        User admin = buildUser(1L, "admin@example.com", "ADMIN");
        User writer = buildUser(2L, "user@example.com", "USER");
        Inquiry inquiry = buildInquiry(10L, writer);

        given(userRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(inquiryRepository.findAllWithUser()).willReturn(List.of(inquiry));

        List<InquiryResponse> responses = inquiryService.getAllInquiriesForAdmin("admin@example.com", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAuthorEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("내 문의 목록은 작성자까지 함께 조회(join fetch)하는 쿼리로 가져온다")
    void getMyInquiries_success() {
        User user = buildUser(1L, "user@example.com", "USER");
        Inquiry inquiry = buildInquiry(10L, user);

        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(inquiryRepository.findAllByUserIdWithUser(1L)).willReturn(List.of(inquiry));

        List<InquiryResponse> responses = inquiryService.getMyInquiries("user@example.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAuthorEmail()).isEqualTo("user@example.com");
        // 목록 매핑 중 작성자를 다시 조회하는 일이 없어야 한다(N+1 방지)
        verify(inquiryRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("관리자가 status를 지정하면 해당 상태의 문의만 조회한다")
    void getAllInquiriesForAdmin_filterByStatus() {
        User admin = buildUser(1L, "admin@example.com", "ADMIN");
        User writer = buildUser(2L, "user@example.com", "USER");
        Inquiry waiting = buildInquiry(10L, writer);

        given(userRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(inquiryRepository.findAllByStatusWithUser(InquiryStatus.WAITING)).willReturn(List.of(waiting));

        List<InquiryResponse> responses =
                inquiryService.getAllInquiriesForAdmin("admin@example.com", InquiryStatus.WAITING);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(InquiryStatus.WAITING);
        verify(inquiryRepository, never()).findAllWithUser();
    }

    @Test
    @DisplayName("존재하지 않는 문의를 조회하면 INQUIRY_NOT_FOUND 예외가 발생한다")
    void getMyInquiryDetail_fail_notFound() {
        User user = buildUser(1L, "user@example.com", "USER");
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(inquiryRepository.findByIdWithUser(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getMyInquiryDetail("user@example.com", 999L))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.INQUIRY_NOT_FOUND);
    }

    @Test
    @DisplayName("관리자가 답변을 등록하면 상태가 ANSWERED로 바뀐다")
    void answerInquiry_success() {
        User admin = buildUser(1L, "admin@example.com", "ADMIN");
        User writer = buildUser(2L, "user@example.com", "USER");
        Inquiry inquiry = buildInquiry(10L, writer);

        given(userRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(inquiryRepository.findByIdWithUser(10L)).willReturn(Optional.of(inquiry));

        InquiryAnswerRequest request = new InquiryAnswerRequest();
        setField(request, "answerContent", "답변입니다.");

        InquiryResponse response = inquiryService.answerInquiry("admin@example.com", 10L, request);

        assertThat(response.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
        assertThat(response.getAnswerContent()).isEqualTo("답변입니다.");
        assertThat(response.getAnsweredAt()).isNotNull();
    }

    @Test
    @DisplayName("일반 회원은 답변을 등록할 수 없다")
    void answerInquiry_fail_notAdmin() {
        User normalUser = buildUser(1L, "user@example.com", "USER");
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(normalUser));

        InquiryAnswerRequest request = new InquiryAnswerRequest();
        setField(request, "answerContent", "답변입니다.");

        assertThatThrownBy(() -> inquiryService.answerInquiry("user@example.com", 10L, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.ADMIN_ONLY);

        verify(inquiryRepository, never()).findByIdWithUser(org.mockito.ArgumentMatchers.anyLong());
    }

    // ---- 리플렉션 헬퍼 ----
    // Builder로만 생성 가능한 엔티티/DTO에 @GeneratedValue id처럼 생성자로 못 넣는 값을 세팅하기 위함
    private void setId(Object target, long id) {
        setFieldOnHierarchy(target, "id", id);
    }

    private void setField(Object target, String fieldName, Object value) {
        setFieldOnHierarchy(target, fieldName, value);
    }

    private void setFieldOnHierarchy(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("필드를 찾을 수 없습니다: " + fieldName);
    }
}
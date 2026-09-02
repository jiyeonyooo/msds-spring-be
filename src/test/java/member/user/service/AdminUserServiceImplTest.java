package member.user.service;

import member.common.exception.MemberErrorCode;
import member.common.exception.MemberException;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserRoleUpdateRequest;
import member.user.dto.AdminUserStatsResponse;
import member.user.dto.UserUpdateRequest;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import resv.repository.ResvRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private ResvRepository resvRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

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

    private void givenAdmin(String email, long id) {
        given(userRepository.findByEmail(email)).willReturn(Optional.of(buildUser(id, email, "ADMIN")));
    }

    @Test
    @DisplayName("일반 회원(USER)은 회원 목록을 조회할 수 없다")
    void getUsers_fail_notAdmin() {
        given(userRepository.findByEmail("user@example.com"))
                .willReturn(Optional.of(buildUser(1L, "user@example.com", "USER")));

        assertThatThrownBy(() -> adminUserService.getUsers("user@example.com", null, null, 0, 20))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.ADMIN_ONLY);

        verify(userRepository, never()).searchForAdmin(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @DisplayName("회원 목록에는 회원별 예약·문의 건수가 함께 담긴다")
    void getUsers_includesActivityCounts() {
        givenAdmin("admin@example.com", 1L);
        User member = buildUser(7L, "member@example.com", "USER");
        given(userRepository.searchForAdmin(
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(member), PageRequest.of(0, 20), 1));
        given(resvRepository.countByMemberIds(List.of(7L))).willReturn(List.<Object[]>of(new Object[]{7L, 3L}));
        given(inquiryRepository.countByUserIds(List.of(7L))).willReturn(List.of());

        AdminUserListResponse response = adminUserService.getUsers("admin@example.com", null, null, 0, 20);

        assertThat(response.totalElements()).isEqualTo(1);
        AdminUserListResponse.AdminUserListItem item = response.userList().getFirst();
        assertThat(item.userId()).isEqualTo(7L);
        assertThat(item.reservationCount()).isEqualTo(3L);
        // 집계 결과에 없는 회원은 0건으로 채운다
        assertThat(item.inquiryCount()).isZero();
    }

    @Test
    @DisplayName("목록의 role 필터는 알 수 없는 값이 오면 필터를 걸지 않는다")
    void getUsers_ignoresUnknownRoleFilter() {
        givenAdmin("admin@example.com", 1L);
        given(userRepository.searchForAdmin(
                ArgumentMatchers.isNull(), ArgumentMatchers.eq("hong"), ArgumentMatchers.any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        AdminUserListResponse response = adminUserService.getUsers("admin@example.com", "MANAGER", "hong", 0, 20);

        assertThat(response.userList()).isEmpty();
    }

    @Test
    @DisplayName("관리자는 회원의 이름과 전화번호를 정정할 수 있다")
    void updateUser_success() {
        givenAdmin("admin@example.com", 1L);
        User member = buildUser(7L, "member@example.com", "USER");
        given(userRepository.findById(7L)).willReturn(Optional.of(member));

        UserUpdateRequest request = new UserUpdateRequest();
        setField(request, "name", "김수정");
        setField(request, "phoneNumber", "010-9999-8888");

        AdminUserDetailResponse response = adminUserService.updateUser("admin@example.com", 7L, request);

        assertThat(response.name()).isEqualTo("김수정");
        assertThat(response.phoneNumber()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("관리자는 일반 회원을 관리자로 승격할 수 있다")
    void changeRole_success() {
        givenAdmin("admin@example.com", 1L);
        User member = buildUser(7L, "member@example.com", "USER");
        given(userRepository.findById(7L)).willReturn(Optional.of(member));

        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        setField(request, "role", "admin");

        AdminUserDetailResponse response = adminUserService.changeRole("admin@example.com", 7L, request);

        // 소문자로 들어와도 정규화해서 저장한다
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(member.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("허용되지 않은 권한 값은 거부한다")
    void changeRole_fail_invalidRole() {
        givenAdmin("admin@example.com", 1L);

        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        setField(request, "role", "SUPER_ADMIN");

        assertThatThrownBy(() -> adminUserService.changeRole("admin@example.com", 7L, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.INVALID_ROLE);

        verify(userRepository, never()).findById(ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("관리자는 본인 계정의 권한을 바꿀 수 없다")
    void changeRole_fail_ownAccount() {
        User admin = buildUser(1L, "admin@example.com", "ADMIN");
        given(userRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));

        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        setField(request, "role", "USER");

        assertThatThrownBy(() -> adminUserService.changeRole("admin@example.com", 1L, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.CANNOT_CHANGE_OWN_ROLE);

        assertThat(admin.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("마지막 남은 관리자는 일반 회원으로 내릴 수 없다")
    void changeRole_fail_lastAdmin() {
        givenAdmin("admin@example.com", 1L);
        User otherAdmin = buildUser(2L, "other@example.com", "ADMIN");
        given(userRepository.findById(2L)).willReturn(Optional.of(otherAdmin));
        given(userRepository.countByRole("ADMIN")).willReturn(1L);

        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        setField(request, "role", "USER");

        assertThatThrownBy(() -> adminUserService.changeRole("admin@example.com", 2L, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.LAST_ADMIN_CANNOT_BE_DEMOTED);

        assertThat(otherAdmin.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("관리자가 둘 이상이면 관리자를 일반 회원으로 내릴 수 있다")
    void changeRole_demoteAdmin_success() {
        givenAdmin("admin@example.com", 1L);
        User otherAdmin = buildUser(2L, "other@example.com", "ADMIN");
        given(userRepository.findById(2L)).willReturn(Optional.of(otherAdmin));
        given(userRepository.countByRole("ADMIN")).willReturn(2L);

        AdminUserRoleUpdateRequest request = new AdminUserRoleUpdateRequest();
        setField(request, "role", "USER");

        AdminUserDetailResponse response = adminUserService.changeRole("admin@example.com", 2L, request);

        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 USER_NOT_FOUND 예외가 발생한다")
    void getUser_fail_notFound() {
        givenAdmin("admin@example.com", 1L);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUser("admin@example.com", 999L))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 통계는 전체·관리자·일반 회원 수와 신규 가입 수를 함께 계산한다")
    void getStats_success() {
        givenAdmin("admin@example.com", 1L);
        given(userRepository.count()).willReturn(10L);
        given(userRepository.countByRole("ADMIN")).willReturn(2L);
        given(userRepository.countByCreatedAtGreaterThanEqual(ArgumentMatchers.any(LocalDateTime.class)))
                .willReturn(1L, 4L);

        AdminUserStatsResponse response = adminUserService.getStats("admin@example.com");

        assertThat(response.totalUsers()).isEqualTo(10L);
        assertThat(response.adminUsers()).isEqualTo(2L);
        assertThat(response.generalUsers()).isEqualTo(8L);
        assertThat(response.newUsersToday()).isEqualTo(1L);
        assertThat(response.newUsersLast7Days()).isEqualTo(4L);
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
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("field not found: " + fieldName);
    }
}

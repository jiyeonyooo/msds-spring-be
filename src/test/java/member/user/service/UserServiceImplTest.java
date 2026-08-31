package member.user.service;

import member.user.domain.User;
import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser() {
        return User.builder()
                .email("Test@Example.com") // 대문자 섞어서 정규화 동작도 같이 확인
                .password("encodedPassword")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("이메일 대소문자와 무관하게 내 정보를 조회할 수 있다")
    void getMyProfile_success_caseInsensitive() {
        User user = sampleUser(); // 엔티티 내부에서 이미 소문자로 정규화되어 저장됨
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

        UserResponse response = userService.getMyProfile("TEST@EXAMPLE.COM");

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    void getMyProfile_fail_notFound() {
        given(userRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile("nobody@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원");
    }

    @Test
    @DisplayName("이름과 전화번호를 부분 수정하면 반영된다")
    void updateMyProfile_success() {
        User user = sampleUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest();
        setField(request, "name", "김철수");
        setField(request, "phoneNumber", "010-9999-8888");

        UserUpdateResponse response = userService.updateMyProfile("test@example.com", request);

        assertThat(response.getName()).isEqualTo("김철수");
        assertThat(response.getPhoneNumber()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("이름만 보내면 전화번호는 기존 값이 유지된다")
    void updateMyProfile_partialUpdate_onlyName() {
        User user = sampleUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest();
        setField(request, "name", "김철수");
        setField(request, "phoneNumber", null);

        UserUpdateResponse response = userService.updateMyProfile("test@example.com", request);

        assertThat(response.getName()).isEqualTo("김철수");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678"); // 원래 값 유지
    }

    @Test
    @DisplayName("비밀번호가 맞으면 회원 탈퇴가 성공한다")
    void deleteUser_success() {
        User user = sampleUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);

        UserDeleteRequest request = new UserDeleteRequest();
        setField(request, "password", "rawPassword");

        userService.deleteUser("test@example.com", request);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 회원 탈퇴 시 예외가 발생하고 삭제되지 않는다")
    void deleteUser_fail_wrongPassword() {
        User user = sampleUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        UserDeleteRequest request = new UserDeleteRequest();
        setField(request, "password", "wrongPassword");

        assertThatThrownBy(() -> userService.deleteUser("test@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");

        verify(userRepository, never()).delete(any());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
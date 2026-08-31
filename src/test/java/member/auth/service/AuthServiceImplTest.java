package member.auth.service;

import member.auth.dto.LoginRequest;
import member.auth.dto.LoginResponse;
import member.auth.dto.SignupRequest;
import member.auth.dto.SignupResponse;
import member.common.exception.MemberErrorCode;
import member.common.exception.MemberException;
import member.user.domain.User;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공 시 이메일이 소문자로 정규화되어 저장된다")
    void signup_success_normalizesEmail() {
        // given
        SignupRequest request = new SignupRequest();
        setField(request, "email", "Test@Example.COM");
        setField(request, "password", "rawPassword");
        setField(request, "name", "홍길동");
        setField(request, "phoneNumber", "010-1234-5678");

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com")
                        && user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입하면 예외가 발생한다")
    void signup_fail_duplicateEmail() {
        SignupRequest request = new SignupRequest();
        setField(request, "email", "dup@example.com");
        setField(request, "password", "pw");
        setField(request, "name", "홍길동");
        setField(request, "phoneNumber", "010-1234-5678");

        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 시 access token을 발급한다")
    void login_success() {
        LoginRequest request = new LoginRequest();
        setField(request, "email", "test@example.com");
        setField(request, "password", "rawPassword");

        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createToken("test@example.com", "USER")).willReturn("dummy-jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("dummy-jwt-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void login_fail_emailNotFound() {
        LoginRequest request = new LoginRequest();
        setField(request, "email", "notfound@example.com");
        setField(request, "password", "pw");

        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 시 예외가 발생한다")
    void login_fail_wrongPassword() {
        LoginRequest request = new LoginRequest();
        setField(request, "email", "test@example.com");
        setField(request, "password", "wrongPassword");

        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.INVALID_CREDENTIALS);
    }

    // DTO에 생성자/세터가 없고 @NoArgsConstructor + private 필드만 있어서
    // 리플렉션으로 값을 채워 테스트용 요청 객체를 만든다.
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
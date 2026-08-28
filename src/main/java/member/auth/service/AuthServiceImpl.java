package member.auth.service;

import lombok.RequiredArgsConstructor;
import member.auth.dto.*;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService 구현체.
 * 회원가입/로그인/로그아웃 처리를 담당한다.
 * refresh token 없이 access token(JWT)만 발급하는 정책을 사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword())) // 반드시 암호화 후 저장
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);

        return new SignupResponse(user.getEmail(), "회원가입이 완료되었습니다.");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return new LoginResponse(accessToken);
    }

    @Override
    public void logout(String authHeader) {
        // refresh token 없이 순수 JWT(access token만) 방식으로 운영하기로 결정했으므로
        // 서버 측에서 별도의 토큰 무효화(블랙리스트 등) 처리를 하지 않는다.
        // 클라이언트가 보관 중인 access token을 삭제하는 것으로 로그아웃이 완료된다.
    }

    // 이메일 대소문자만 다른 계정을 서로 다른 계정으로 취급하지 않도록 소문자로 통일
    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}
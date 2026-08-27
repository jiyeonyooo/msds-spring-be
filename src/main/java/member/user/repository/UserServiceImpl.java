package member.user.repository;

import member.user.domain.User;
import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;
import lombok.RequiredArgsConstructor;
import member.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserUpdateResponse updateMyProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.updateProfile(request.getName(), request.getPhoneNumber());
        return UserUpdateResponse.of(user, "회원 정보가 성공적으로 수정되었습니다.");
    }

    @Override
    @Transactional
    public void deleteUser(String email, UserDeleteRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 본인 확인용 비밀번호 일치 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }
}
package member.user.service;

import member.common.exception.MemberErrorCode;
import member.common.exception.MemberException;
import member.user.domain.User;
import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;
import member.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService 구현체.
 * 클래스 레벨 기본 트랜잭션은 readOnly=true로 두고, 실제 변경이 일어나는
 * 메서드에만 @Transactional을 개별로 덮어써서 쓰기 트랜잭션으로 전환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getMyProfile(String email) {
        // 이메일 대소문자 정책(항상 소문자 저장)에 맞춰 조회 전 정규화
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserUpdateResponse updateMyProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));

        // 엔티티 내부 도메인 로직 호출 (null/공백 필드는 무시하고 부분 반영)
        user.updateProfile(request.getName(), request.getPhoneNumber());
        // updatedAt은 User 엔티티의 @PreUpdate에서 자동 갱신됨 (여기서 별도 처리 불필요)
        return UserUpdateResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email, UserDeleteRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));

        // 본인 확인용 비밀번호 일치 검증 (탈퇴 오조작/도용 방지)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new MemberException(MemberErrorCode.PASSWORD_MISMATCH);
        }

        // 정책: hard delete (탈퇴 이력 보관 없이 즉시 완전 삭제, 이메일 즉시 재사용 가능)
        // User.inquiries의 cascade = REMOVE 설정에 의해 이 회원이 작성한 문의도 함께 삭제된다.
        userRepository.delete(user);
    }

    // 이메일 대소문자만 다른 계정을 서로 다른 계정으로 취급하지 않도록 소문자로 통일
    private String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}
package member.user.service;

import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;

/**
 * 인증된 사용자 본인의 정보 조회/수정/탈퇴를 담당하는 서비스.
 * 모든 메서드의 email 파라미터는 JWT에서 추출한 로그인 식별자(이메일)를 그대로 전달받는다.
 */
public interface UserService {

    // 내 정보 조회
    UserResponse getMyProfile(String email);

    // 내 정보 부분 수정 (이름, 전화번호)
    UserUpdateResponse updateMyProfile(String email, UserUpdateRequest request);

    // 회원 탈퇴 (비밀번호 재확인 후 hard delete)
    void deleteUser(String email, UserDeleteRequest request);
}
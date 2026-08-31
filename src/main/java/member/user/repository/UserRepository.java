package member.user.repository;

import member.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근 계층(JPA Repository).
 * 기본 CRUD는 JpaRepository가 제공하고, 이메일 기반 조회만 추가로 정의한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 ID인 이메일로 회원 조회. 호출 측에서 항상 정규화(소문자)된 값을 넘긴다고 가정.
    Optional<User> findByEmail(String email);

    // 회원가입 시 이메일 중복 여부 확인용.
    boolean existsByEmail(String email);
}
package member.user.repository;

import member.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근 계층(JPA Repository).
 * 기본 CRUD는 JpaRepository가 제공하고, 이메일 기반 조회와 관리자 회원 관리용 검색/집계만 추가로 정의한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 ID인 이메일로 회원 조회. 호출 측에서 항상 정규화(소문자)된 값을 넘긴다고 가정.
    Optional<User> findByEmail(String email);

    // 회원가입 시 이메일 중복 여부 확인용.
    boolean existsByEmail(String email);

    /**
     * 관리자 회원 목록 검색. role과 keyword는 값이 없으면(null) 해당 조건을 건너뛴다.
     * keyword는 이메일/이름/전화번호를 부분 일치로 훑는다(정렬은 Pageable로 받는다).
     */
    @Query("""
            select user from User user
            where (:role is null or user.role = :role)
              and (:keyword is null
                   or lower(user.email) like lower(concat('%', :keyword, '%'))
                   or lower(user.name) like lower(concat('%', :keyword, '%'))
                   or user.phoneNumber like concat('%', :keyword, '%'))
            """)
    Page<User> searchForAdmin(
            @Param("role") String role,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 대시보드 집계용. 권한별 회원 수.
    long countByRole(String role);

    // 대시보드 집계용. 특정 시각 이후 가입한 회원 수(오늘/최근 7일 신규 가입).
    long countByCreatedAtGreaterThanEqual(LocalDateTime from);
}

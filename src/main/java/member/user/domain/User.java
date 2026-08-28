package member.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 회원(User) 도메인 엔티티.
 * users 테이블과 매핑되며, 회원의 상태와 상태 변경 로직(도메인 로직)을 캡슐화한다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 필요, 외부에서는 Builder로만 생성 강제
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 ID로 사용되는 이메일. 대소문자 구분 없이 유일해야 하므로 항상 정규화(소문자) 후 저장됨
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호 (평문 저장 금지)

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 20)
    private String role; // 권한 구분: "USER" 또는 "ADMIN"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 최초 생성 시각 (수정 불가)

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 마지막 수정 시각 (PreUpdate 콜백으로 자동 갱신)

    /**
     * 회원 생성자.
     * - email은 소문자로 정규화하여 저장
     * - role이 없으면 기본값 "USER" 부여
     * - createdAt/updatedAt은 생성 시점 값으로 초기화
     */
    @Builder
    public User(String email, String password, String name, String phoneNumber, String role) {
        this.email = normalizeEmail(email);
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = (role != null) ? role : "USER";
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 회원 정보(이름, 전화번호) 수정.
     * null 또는 공백이 아닌 값만 반영하며, JPA 변경 감지(Dirty Checking)에 의해
     * 트랜잭션 커밋 시 자동으로 UPDATE 쿼리가 반영된다.
     */
    public void updateProfile(String name, String phoneNumber) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * 엔티티가 UPDATE되기 직전에 JPA가 자동 호출.
     * updatedAt을 현재 시각으로 갱신하여 마지막 수정 시각을 항상 최신 상태로 유지한다.
     */
    @PreUpdate
    private void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이메일 정규화: 앞뒤 공백 제거 + 소문자 변환.
     * 대소문자만 다른 이메일이 서로 다른 계정으로 취급되는 것을 방지.
     */
    private static String normalizeEmail(String email) {
        return (email == null) ? null : email.trim().toLowerCase();
    }
}
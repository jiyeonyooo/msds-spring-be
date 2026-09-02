package member.user.repository;

import member.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 회원 검색 쿼리(searchForAdmin)를 실제 DB(H2)로 확인한다.
 * role/keyword가 null일 때 조건을 건너뛰는 동작은 JPQL 파라미터 처리에 달려 있어 단위 테스트로는 검증되지 않는다.
 */
@DataJpaTest
@EntityScan(basePackages = {"member.user.domain", "member.inquiry.domain"})
@EnableJpaRepositories(basePackages = "member.user.repository")
// 운영 설정(application.properties)이 요구하는 환경변수 자리를 채워 테스트 컨텍스트가 뜨게 한다.
// 실제 데이터소스는 @DataJpaTest가 H2로 교체하므로 값 자체는 의미가 없다.
@TestPropertySource(properties = {"DB_URL=", "DB_USERNAME=sa", "DB_PASSWORD=", "JWT_SECRET=test-secret"})
class UserRepositoryTest {

    // 이 프로젝트의 진입점(MeditationApplication)은 member 패키지의 상위가 아니라 @DataJpaTest가 설정을 찾지 못한다.
    // 다른 테스트에 영향을 주지 않도록 이 테스트 전용 최소 설정을 여기에 둔다.
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfiguration {
    }

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(user("hong@example.com", "홍길동", "010-1111-2222", "USER"));
        userRepository.save(user("minji@example.com", "김민지", "010-3333-4444", "USER"));
        userRepository.save(user("admin@example.com", "관리자", "010-5555-6666", "ADMIN"));
        userRepository.flush();
    }

    private User user(String email, String name, String phoneNumber, String role) {
        return User.builder()
                .email(email)
                .password("encoded")
                .name(name)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
    }

    @Test
    @DisplayName("role과 keyword가 모두 없으면 전체 회원을 조회한다")
    void searchForAdmin_noFilter() {
        Page<User> result = userRepository.searchForAdmin(null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("role 필터는 해당 권한의 회원만 조회한다")
    void searchForAdmin_byRole() {
        Page<User> result = userRepository.searchForAdmin("ADMIN", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).singleElement()
                .satisfies(found -> assertThat(found.getEmail()).isEqualTo("admin@example.com"));
    }

    @Test
    @DisplayName("keyword는 이메일·이름·전화번호를 대소문자 구분 없이 부분 일치로 검색한다")
    void searchForAdmin_byKeyword() {
        assertThat(userRepository.searchForAdmin(null, "HONG", PageRequest.of(0, 10)).getContent())
                .singleElement()
                .satisfies(found -> assertThat(found.getName()).isEqualTo("홍길동"));
        assertThat(userRepository.searchForAdmin(null, "김민지", PageRequest.of(0, 10)).getContent())
                .hasSize(1);
        assertThat(userRepository.searchForAdmin(null, "3333", PageRequest.of(0, 10)).getContent())
                .hasSize(1);
    }

    @Test
    @DisplayName("role과 keyword는 함께 적용된다")
    void searchForAdmin_byRoleAndKeyword() {
        assertThat(userRepository.searchForAdmin("USER", "example.com", PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(2);
        assertThat(userRepository.searchForAdmin("ADMIN", "hong", PageRequest.of(0, 10)).getContent())
                .isEmpty();
    }

    @Test
    @DisplayName("권한별 회원 수와 가입 시각 기준 신규 가입 수를 센다")
    void countsForStats() {
        assertThat(userRepository.countByRole("ADMIN")).isEqualTo(1);
        assertThat(userRepository.countByRole("USER")).isEqualTo(2);
        assertThat(userRepository.countByCreatedAtGreaterThanEqual(LocalDateTime.now().minusMinutes(1))).isEqualTo(3);
        assertThat(userRepository.countByCreatedAtGreaterThanEqual(LocalDateTime.now().plusMinutes(1))).isZero();
    }
}

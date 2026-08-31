package member.user.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User - Inquiry 연관관계 매핑 검증.
 *
 * 회원 탈퇴(hard delete) 시 그 회원이 작성한 문의가 함께 삭제되지 않으면
 * inquiries.user_id FK 제약조건 위반으로 탈퇴 자체가 실패한다.
 * 현재 프로젝트에 테스트용 DB 설정이 없어 실제 delete까지 검증할 수 없으므로,
 * 최소한 cascade 설정이 실수로 제거되는 것을 막는 회귀 방지 테스트로 둔다.
 */
class UserMappingTest {

    @Test
    @DisplayName("User.inquiries는 Inquiry가 주인인 1:N 연관관계이며 삭제가 전이되도록 설정되어 있다")
    void inquiriesMapping_hasRemoveCascade() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("inquiries");
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);

        assertThat(oneToMany).isNotNull();
        // 연관관계의 주인은 FK(user_id)를 가진 Inquiry.user
        assertThat(oneToMany.mappedBy()).isEqualTo("user");
        assertThat(oneToMany.cascade()).contains(CascadeType.REMOVE);
        assertThat(oneToMany.orphanRemoval()).isTrue();
    }
}

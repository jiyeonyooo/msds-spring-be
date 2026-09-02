package member.user.service;

import member.user.domain.User;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AdminUserService service = new AdminUserService(userRepository);

    @Test
    void 회원_목록과_역할별_수를_조회한다() {
        User user = user("guest@msds.com", "박시우", "USER");
        when(userRepository.searchForAdmin(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userRepository.countByRole("USER")).thenReturn(1L);
        when(userRepository.countByRole("ADMIN")).thenReturn(2L);

        AdminUserListResponse result = service.getUsers("박", "user", 0, 20);

        assertThat(result.content()).singleElement().satisfies(item -> {
            assertThat(item.email()).isEqualTo("guest@msds.com");
            assertThat(item.role()).isEqualTo("USER");
        });
        assertThat(result.userCount()).isEqualTo(1);
        assertThat(result.adminCount()).isEqualTo(2);
    }

    @Test
    void 회원_상세를_조회한다() {
        User user = user("guest@msds.com", "박시우", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDetailResponse result = service.getUser(1L);

        assertThat(result.name()).isEqualTo("박시우");
    }

    @Test
    void 존재하지_않는_회원은_조회할_수_없다() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    private User user(String email, String name, String role) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .name(name)
                .phoneNumber("010-1234-5678")
                .role(role)
                .build();
    }
}

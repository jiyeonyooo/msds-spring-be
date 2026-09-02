package member.inquiry.config;

import member.inquiry.domain.Inquiry;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InquiryDemoDataInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final InquiryRepository inquiryRepository = mock(InquiryRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final InquiryDemoDataInitializer initializer = new InquiryDemoDataInitializer(
            userRepository,
            inquiryRepository,
            passwordEncoder,
            "admin@msds.com",
            "Admin2026!"
    );

    @Test
    void 데이터가_없으면_관리자와_문의_샘플을_생성한다() {
        when(userRepository.findByEmail("admin@msds.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("startup@msds.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inquiryRepository.count()).thenReturn(0L);

        initializer.run(new DefaultApplicationArguments());

        verify(userRepository, times(2)).save(any(User.class));
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    void 기존_문의가_있으면_문의_데이터를_보존한다() {
        User admin = user("admin@msds.com", "ADMIN");
        User guest = user("startup@msds.local", "USER");
        when(userRepository.findByEmail("admin@msds.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("startup@msds.local")).thenReturn(Optional.of(guest));
        when(inquiryRepository.count()).thenReturn(1L);

        initializer.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(User.class));
        verify(inquiryRepository, never()).save(any(Inquiry.class));
    }

    private User user(String email, String role) {
        User user = User.builder()
                .email(email)
                .password("encoded")
                .name("사용자")
                .phoneNumber("010-0000-0000")
                .role(role)
                .build();
        assertThat(user.getRole()).isEqualTo(role);
        return user;
    }
}

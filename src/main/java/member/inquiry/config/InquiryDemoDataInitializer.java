package member.inquiry.config;

import member.inquiry.domain.Inquiry;
import member.inquiry.repository.InquiryRepository;
import member.user.domain.User;
import member.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.inquiry.demo", name = "enabled", havingValue = "true")
public class InquiryDemoDataInitializer implements ApplicationRunner {

    private static final String DEMO_GUEST_EMAIL = "startup@msds.local";

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public InquiryDemoDataInitializer(
            UserRepository userRepository,
            InquiryRepository inquiryRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.inquiry.demo.admin-email:admin@msds.com}") String adminEmail,
            @Value("${app.inquiry.demo.admin-password:Admin2026!}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.inquiryRepository = inquiryRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(adminEmail)
                .orElseGet(() -> userRepository.save(user(
                        adminEmail,
                        adminPassword,
                        "MSDS 관리자",
                        "010-0000-0000",
                        "ADMIN"
                )));

        User guest = userRepository.findByEmail(DEMO_GUEST_EMAIL)
                .orElseGet(() -> userRepository.save(user(
                        DEMO_GUEST_EMAIL,
                        "Guest2026!",
                        "startup",
                        "010-1111-2222",
                        "USER"
                )));

        if (inquiryRepository.count() == 0) {
            inquiryRepository.save(Inquiry.builder()
                    .user(guest)
                    .title("객실 내 디지털 디톡스 안내가 궁금해요")
                    .content("안녕하세요. MSDS에서 디지털 디톡스 스테이를 계획하고 있습니다.\n\n"
                            + "객실에서 휴대폰을 따로 보관하거나 사용 시간을 제한하는 별도의 안내가 있는지 궁금합니다. "
                            + "강제 운영인지, 원할 때만 참여할 수 있는 프로그램인지도 알려 주세요.")
                    .build());
        }
    }

    private User user(String email, String password, String name, String phoneNumber, String role) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
    }
}

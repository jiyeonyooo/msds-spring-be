package member.user.service;

import lombok.RequiredArgsConstructor;
import member.user.domain.User;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.dto.AdminUserSummaryResponse;
import member.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;

    public AdminUserListResponse getUsers(String keyword, String role, int page, int size) {
        String normalizedKeyword = normalize(keyword);
        String normalizedRole = normalizeRole(role);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<AdminUserSummaryResponse> users = userRepository
                .searchForAdmin(normalizedKeyword, normalizedRole, pageable)
                .map(AdminUserSummaryResponse::from);

        return AdminUserListResponse.of(
                users,
                userRepository.countByRole("USER"),
                userRepository.countByRole("ADMIN")
        );
    }

    public AdminUserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "회원을 찾을 수 없습니다."));
        return AdminUserDetailResponse.from(user);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeRole(String role) {
        String normalized = normalize(role);
        if (normalized == null) {
            return null;
        }
        String upper = normalized.toUpperCase();
        if (!upper.equals("USER") && !upper.equals("ADMIN")) {
            throw new IllegalArgumentException("역할은 USER 또는 ADMIN이어야 합니다.");
        }
        return upper;
    }
}

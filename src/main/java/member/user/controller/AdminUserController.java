package member.user.controller;

import global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import member.user.dto.AdminUserDetailResponse;
import member.user.dto.AdminUserListResponse;
import member.user.service.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<AdminUserListResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                "회원 목록을 조회했습니다.",
                adminUserService.getUsers(keyword, role, page, size)
        );
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.success(
                "회원 상세 정보를 조회했습니다.",
                adminUserService.getUser(userId)
        );
    }
}

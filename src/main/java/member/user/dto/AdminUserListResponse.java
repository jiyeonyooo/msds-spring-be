package member.user.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserListResponse(
        List<AdminUserSummaryResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        long userCount,
        long adminCount
) {
    public static AdminUserListResponse of(Page<AdminUserSummaryResponse> page, long userCount, long adminCount) {
        return new AdminUserListResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                userCount,
                adminCount
        );
    }
}

package member.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// GET /api/admin/users/stats 응답. 관리자 대시보드 상단 집계 타일용.
public record AdminUserStatsResponse(
        @JsonProperty("total_users") long totalUsers,
        @JsonProperty("admin_users") long adminUsers,
        @JsonProperty("general_users") long generalUsers,
        @JsonProperty("new_users_today") long newUsersToday,
        @JsonProperty("new_users_last_7_days") long newUsersLast7Days
) { }

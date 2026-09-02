package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/admin/users 응답.
 * 페이징 봉투 형태는 관리자 예약 목록(ResvListResponseDTO)과 동일하게 맞춰,
 * 관리자 화면이 목록 API마다 다른 규격을 다루지 않아도 되게 한다.
 */
public record AdminUserListResponse(
        @JsonProperty("user_list") List<AdminUserListItem> userList,
        @JsonProperty("page_num") int pageNum,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages
) {
    public static AdminUserListResponse of(Page<AdminUserListItem> page) {
        return new AdminUserListResponse(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public record AdminUserListItem(
            @JsonProperty("user_id") Long userId,
            String email,
            String name,
            @JsonProperty("phone_number") String phoneNumber,
            String role,

            // 회원별 예약/문의 건수. 목록 행마다 조회하지 않고 한 번의 집계 쿼리로 채운다.
            @JsonProperty("reservation_count") long reservationCount,
            @JsonProperty("inquiry_count") long inquiryCount,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonProperty("created_at") LocalDateTime createdAt
    ) { }
}

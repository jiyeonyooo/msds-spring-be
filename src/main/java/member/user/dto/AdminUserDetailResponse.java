package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import member.user.domain.User;

import java.time.LocalDateTime;

// GET /api/admin/users/{userId} 응답. 회원 기본 정보와 활동 요약(예약/문의 건수)을 함께 담는다.
public record AdminUserDetailResponse(
        @JsonProperty("user_id") Long userId,
        String email,
        String name,
        @JsonProperty("phone_number") String phoneNumber,
        String role,
        @JsonProperty("reservation_count") long reservationCount,
        @JsonProperty("inquiry_count") long inquiryCount,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("created_at") LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
    public static AdminUserDetailResponse of(User user, long reservationCount, long inquiryCount) {
        return new AdminUserDetailResponse(
                user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber(), user.getRole(),
                reservationCount, inquiryCount, user.getCreatedAt(), user.getUpdatedAt());
    }
}

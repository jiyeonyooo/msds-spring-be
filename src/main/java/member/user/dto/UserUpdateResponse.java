package member.user.dto;

import member.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateResponse {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private String message;

    public static UserUpdateResponse of(User user, String message) {
        return new UserUpdateResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                message
        );
    }
}
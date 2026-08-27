package member.user.service;

import member.user.dto.UserDeleteRequest;
import member.user.dto.UserResponse;
import member.user.dto.UserUpdateRequest;
import member.user.dto.UserUpdateResponse;

public interface UserService {
    UserResponse getMyProfile(String email);
    UserUpdateResponse updateMyProfile(String email, UserUpdateRequest request);
    void deleteUser(String email, UserDeleteRequest request);
}
package member.auth.service;

import member.auth.dto.*;

public interface AuthService {
    SignupResponse signup(SignupRequest request);
    LoginResponse login(LoginRequest request);
    void logout(String authHeader, LogoutRequest request);
}
package com.doan.backend.modules.auth.service;

import com.doan.backend.modules.auth.dto.request.ChangePasswordRequest;
import com.doan.backend.modules.auth.dto.request.LoginRequest;
import com.doan.backend.modules.auth.dto.request.RegisterRequest;
import com.doan.backend.modules.auth.dto.response.LoginResponse;
import com.doan.backend.modules.auth.dto.response.UserInfoResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserInfoResponse me();

    void changePassword(ChangePasswordRequest request);
}

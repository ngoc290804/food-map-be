package com.doan.backend.modules.auth.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.auth.dto.request.LoginRequest;
import com.doan.backend.modules.auth.dto.request.RegisterRequest;
import com.doan.backend.modules.auth.dto.response.LoginResponse;
import com.doan.backend.modules.auth.dto.response.ProfileResponse;
import com.doan.backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> me() {
        return ApiResponse.success(authService.me());
    }
}

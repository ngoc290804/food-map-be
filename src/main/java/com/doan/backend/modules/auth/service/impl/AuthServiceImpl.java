package com.doan.backend.modules.auth.service.impl;

import com.doan.backend.common.constant.AppConstants;
import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.modules.auth.dto.request.ChangePasswordRequest;
import com.doan.backend.modules.auth.dto.request.LoginRequest;
import com.doan.backend.modules.auth.dto.request.RegisterRequest;
import com.doan.backend.modules.auth.dto.response.LoginResponse;
import com.doan.backend.modules.auth.dto.response.UserInfoResponse;
import com.doan.backend.modules.auth.service.AuthService;
import com.doan.backend.modules.user.entity.Role;
import com.doan.backend.modules.user.entity.User;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.CustomUserDetails;
import com.doan.backend.security.JwtTokenProvider;
import com.doan.backend.security.SecurityUtils;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username da ton tai");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email da ton tai");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AppConstants.STATUS_ACTIVE);
        user.setRoleCode("khach");
        User savedUser = userRepository.save(user);

        return buildLoginResponse(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập/email hoặc mật khẩu không đúng"));
        if (!isPasswordValid(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Tên đăng nhập/email hoặc mật khẩu không đúng");
        }
        upgradeLegacyPasswordIfNeeded(user, request.getPassword());
        return buildLoginResponse(user);
    }

    @Override
    public UserInfoResponse me() {
        UUID userId = SecurityUtils.getCurrentUser()
                .map(CustomUserDetails::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong co thong tin nguoi dung"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));

        return buildUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("confirmPassword khong khop");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("newPassword phai khac currentPassword");
        }

        UUID userId = SecurityUtils.getCurrentUser()
                .map(CustomUserDetails::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong co thong tin nguoi dung"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("currentPassword khong dung");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(userDetails))
                .tokenType("Bearer")
                .user(buildUserInfoResponse(user))
                .build();
    }

    private boolean isPasswordValid(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return isBcryptHash(storedPassword)
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }

    private void upgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        if (isBcryptHash(user.getPassword())) {
            return;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private UserInfoResponse buildUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roles(extractRoles(user))
                .build();
    }

    private Set<String> extractRoles(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
        if (roles.isEmpty() && user.getRoleCode() != null && !user.getRoleCode().isBlank()) {
            roles.add(user.getRoleCode());
        }
        return roles;
    }
}

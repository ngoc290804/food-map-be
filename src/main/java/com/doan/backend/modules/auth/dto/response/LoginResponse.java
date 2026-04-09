package com.doan.backend.modules.auth.dto.response;

import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final String accessToken;
    private final String tokenType;
    private final UUID userId;
    private final String username;
    private final String fullName;
    private final Set<String> roles;
}

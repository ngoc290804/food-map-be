package com.doan.backend.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final String accessToken;
    private final String tokenType;
    private final UserInfoResponse user;
}

package com.doan.backend.modules.auth.dto.response;

import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private final UUID id;
    private final String username;
    private final String email;
    private final String fullName;
    private final String status;
    private final Set<String> roles;
}

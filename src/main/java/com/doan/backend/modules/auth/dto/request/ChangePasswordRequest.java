package com.doan.backend.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "currentPassword khong duoc de trong")
    private String currentPassword;

    @NotBlank(message = "newPassword khong duoc de trong")
    @Size(min = 6, max = 100, message = "newPassword phai tu 6 den 100 ky tu")
    private String newPassword;

    @NotBlank(message = "confirmPassword khong duoc de trong")
    private String confirmPassword;
}

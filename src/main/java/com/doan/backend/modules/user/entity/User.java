package com.doan.backend.modules.user.entity;

import com.doan.backend.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "nguoi_dung")
public class User extends BaseEntity {

    @Column(name = "ten", nullable = false, length = 255)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mat_khau", nullable = false, length = 255)
    private String password;

    @Column(name = "ten", nullable = false, length = 255, insertable = false, updatable = false)
    private String fullName;

    @Transient
    private String status;

    @Transient
    private Set<Role> roles = new HashSet<>();
}

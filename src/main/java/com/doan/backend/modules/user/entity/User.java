package com.doan.backend.modules.user.entity;

import com.doan.backend.common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbltaikhoan")
@AttributeOverride(name = "createdAt", column = @Column(name = "ngaytao", updatable = false))
public class User extends BaseEntity {

    @Column(name = "bietdanh", length = 100)
    private String username;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "matkhau", length = 255)
    private String password;

    @Column(name = "bietdanh", length = 100, insertable = false, updatable = false)
    private String fullName;

    @Column(name = "phanquyen", length = 20)
    private String roleCode;

    @Transient
    private String status = "ACTIVE";

    @Transient
    private Set<Role> roles = new HashSet<>();

    @PostLoad
    void syncTransientFields() {
        status = "ACTIVE";
        roles = new HashSet<>();
        if (roleCode != null && !roleCode.isBlank()) {
            Role role = new Role();
            role.setCode(roleCode);
            role.setName(roleCode);
            roles.add(role);
        }
    }
}

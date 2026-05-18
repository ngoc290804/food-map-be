package com.doan.backend.security;

import com.doan.backend.modules.user.entity.Role;
import com.doan.backend.modules.user.entity.User;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        Stream<String> roleCodes = user.getRoles().isEmpty() && user.getRoleCode() != null
                ? Stream.of(user.getRoleCode())
                : user.getRoles().stream().map(Role::getCode);
        this.authorities = roleCodes
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}

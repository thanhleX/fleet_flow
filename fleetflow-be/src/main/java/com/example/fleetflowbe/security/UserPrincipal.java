package com.example.fleetflowbe.security;

import com.example.fleetflowbe.common.constants.Role;
import com.example.fleetflowbe.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Role role;
    private final boolean active;
    private final Long driverId; // Associated driverId if this user is a DRIVER

    public static UserPrincipal create(User user, Long driverId) {
        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .driverId(driverId)
                .build();
    }

    public Role getRole() {
        return role != null ? role.toCanonical() : Role.ROLE_STAFF;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role canonical = getRole();
        return List.of(new SimpleGrantedAuthority(canonical.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}


package com.hrms.security;

import com.hrms.entity.CompanyMembership;
import com.hrms.entity.User;
import com.hrms.enums.Role;
import com.hrms.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final UUID activeCompanyId;
    private final Role role;
    private final UserStatus status;
    private final boolean isVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user, CompanyMembership activeMembership) {
        Role role = activeMembership != null ? activeMembership.getRole() : Role.EMPLOYEE;
        UUID companyId = activeMembership != null ? activeMembership.getCompany().getId() : null;

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .activeCompanyId(companyId)
                .role(role)
                .status(user.getStatus())
                .isVerified(user.isVerified())
                .authorities(authorities)
                .build();
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
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified && status == UserStatus.ACTIVE;
    }
}

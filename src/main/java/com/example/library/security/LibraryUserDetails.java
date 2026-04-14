package com.example.library.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LibraryUserDetails implements UserDetails {

    /** 与 {@link TenantRoles#AUTHORITY_PLATFORM_ADMIN} 同值，便于沿用既有引用 */
    public static final String AUTHORITY_PLATFORM_ADMIN = TenantRoles.AUTHORITY_PLATFORM_ADMIN;

    private final Long tenantId;
    private final String tenantCode;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final String tenantRole;
    private final boolean platformAdmin;
    private final List<GrantedAuthority> authorities;

    public LibraryUserDetails(
            Long tenantId,
            String tenantCode,
            String username,
            String password,
            boolean enabled,
            String tenantRole,
            boolean platformAdmin) {
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.tenantRole = TenantRoles.normalize(tenantRole);
        this.platformAdmin = platformAdmin;
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority(this.tenantRole));
        if (platformAdmin) {
            auths.add(new SimpleGrantedAuthority(TenantRoles.AUTHORITY_PLATFORM_ADMIN));
        }
        this.authorities = List.copyOf(auths);
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public String getTenantRole() {
        return tenantRole;
    }

    public boolean isPlatformAdmin() {
        return platformAdmin;
    }

    /** 租户管理员（{@link TenantRoles#ROLE_TENANT_ADMIN}，含自 {@link TenantRoles#LEGACY_ROLE_ADMIN} 规范化） */
    public boolean isTenantAdmin() {
        return TenantRoles.isTenantAdminRole(tenantRole);
    }

    /** 普通成员 */
    public boolean isTenantMember() {
        return TenantRoles.isMemberRole(tenantRole);
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

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

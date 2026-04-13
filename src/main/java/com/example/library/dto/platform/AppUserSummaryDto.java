package com.example.library.dto.platform;

import com.example.library.entity.AppUser;

public class AppUserSummaryDto {

    private Long id;
    private Long tenantId;
    private String username;
    private String role;
    private boolean enabled;
    private boolean platformAdmin;

    public static AppUserSummaryDto fromEntity(AppUser u) {
        AppUserSummaryDto d = new AppUserSummaryDto();
        d.setId(u.getId());
        d.setTenantId(u.getTenantId());
        d.setUsername(u.getUsername());
        d.setRole(u.getRole());
        d.setEnabled(u.isEnabled());
        d.setPlatformAdmin(u.isPlatformAdmin());
        return d;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPlatformAdmin() {
        return platformAdmin;
    }

    public void setPlatformAdmin(boolean platformAdmin) {
        this.platformAdmin = platformAdmin;
    }
}

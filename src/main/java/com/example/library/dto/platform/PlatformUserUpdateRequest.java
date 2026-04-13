package com.example.library.dto.platform;

public class PlatformUserUpdateRequest {

    private String role;
    private Boolean enabled;
    private Boolean platformAdmin;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getPlatformAdmin() {
        return platformAdmin;
    }

    public void setPlatformAdmin(Boolean platformAdmin) {
        this.platformAdmin = platformAdmin;
    }
}

package com.example.library.dto;

import com.example.library.security.LibraryUserDetails;

public class UserInfoResponse {

    private String username;
    private String role;
    private String tenantCode;
    private boolean platformAdmin;
    /** 租户管理员，与 {@link LibraryUserDetails#isTenantAdmin()} 一致 */
    private boolean tenantAdmin;

    public UserInfoResponse() {
    }

    public static UserInfoResponse fromLibraryUser(LibraryUserDetails lud) {
        UserInfoResponse r = new UserInfoResponse();
        r.setUsername(lud.getUsername());
        r.setRole(lud.getTenantRole());
        r.setTenantCode(lud.getTenantCode());
        r.setPlatformAdmin(lud.isPlatformAdmin());
        r.setTenantAdmin(lud.isTenantAdmin());
        return r;
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

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public boolean isPlatformAdmin() {
        return platformAdmin;
    }

    public void setPlatformAdmin(boolean platformAdmin) {
        this.platformAdmin = platformAdmin;
    }

    public boolean isTenantAdmin() {
        return tenantAdmin;
    }

    public void setTenantAdmin(boolean tenantAdmin) {
        this.tenantAdmin = tenantAdmin;
    }
}

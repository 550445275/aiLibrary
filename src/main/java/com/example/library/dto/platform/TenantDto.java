package com.example.library.dto.platform;

import com.example.library.entity.Tenant;

import java.time.LocalDateTime;

public class TenantDto {

    private Long id;
    private String code;
    private String name;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static TenantDto fromEntity(Tenant t) {
        if (t == null) {
            return null;
        }
        TenantDto d = new TenantDto();
        d.setId(t.getId());
        d.setCode(t.getCode());
        d.setName(t.getName());
        d.setEnabled(t.isEnabled());
        d.setCreatedAt(t.getCreatedAt());
        return d;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

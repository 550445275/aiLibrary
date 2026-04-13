package com.example.library.service.platform;

import com.example.library.dto.platform.AppUserSummaryDto;
import com.example.library.dto.platform.PlatformUserCreateRequest;
import com.example.library.dto.platform.PlatformUserUpdateRequest;
import com.example.library.entity.AppUser;
import com.example.library.mapper.AppUserMapper;
import com.example.library.mapper.TenantMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class PlatformTenantUserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ROLE_ADMIN", "ROLE_USER");

    private final AppUserMapper appUserMapper;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;

    public PlatformTenantUserService(
            AppUserMapper appUserMapper, TenantMapper tenantMapper, PasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.tenantMapper = tenantMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private void requireTenant(Long tenantId) {
        if (tenantMapper.selectById(tenantId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "租户不存在");
        }
    }

    @Transactional(readOnly = true)
    public List<AppUserSummaryDto> listByTenant(Long tenantId) {
        requireTenant(tenantId);
        return appUserMapper.listByTenantId(tenantId).stream()
                .map(AppUserSummaryDto::fromEntity)
                .toList();
    }

    @Transactional
    public AppUserSummaryDto create(Long tenantId, PlatformUserCreateRequest req) {
        requireTenant(tenantId);
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()
                || req.getRole() == null || req.getRole().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名、密码、角色不能为空");
        }
        String role = req.getRole().trim();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色仅支持 ROLE_ADMIN 或 ROLE_USER");
        }
        AppUser u = new AppUser();
        u.setTenantId(tenantId);
        u.setUsername(req.getUsername().trim());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(role);
        u.setEnabled(req.getEnabled() == null || req.getEnabled());
        u.setPlatformAdmin(false);
        try {
            appUserMapper.insert(u);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名在该租户下已存在或数据无效");
        }
        AppUser saved = appUserMapper.selectById(u.getId());
        return AppUserSummaryDto.fromEntity(saved);
    }

    @Transactional
    public AppUserSummaryDto update(Long tenantId, Long userId, PlatformUserUpdateRequest req) {
        requireTenant(tenantId);
        AppUser existing = appUserMapper.selectById(userId);
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (req.getRole() != null && !req.getRole().isBlank()) {
            String role = req.getRole().trim();
            if (!ALLOWED_ROLES.contains(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色仅支持 ROLE_ADMIN 或 ROLE_USER");
            }
            existing.setRole(role);
        }
        if (req.getEnabled() != null) {
            existing.setEnabled(req.getEnabled());
        }
        if (req.getPlatformAdmin() != null) {
            existing.setPlatformAdmin(req.getPlatformAdmin());
        }
        appUserMapper.updateForPlatform(existing);
        return AppUserSummaryDto.fromEntity(appUserMapper.selectById(userId));
    }
}

package com.example.library.controller.api.platform;

import com.example.library.dto.platform.AppUserSummaryDto;
import com.example.library.dto.platform.PlatformUserCreateRequest;
import com.example.library.dto.platform.PlatformUserUpdateRequest;
import com.example.library.service.platform.PlatformTenantUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/tenants/{tenantId}/users")
@PreAuthorize("hasAuthority(T(com.example.library.security.TenantRoles).AUTHORITY_PLATFORM_ADMIN)")
public class PlatformTenantUserController {

    private final PlatformTenantUserService platformTenantUserService;

    public PlatformTenantUserController(PlatformTenantUserService platformTenantUserService) {
        this.platformTenantUserService = platformTenantUserService;
    }

    @GetMapping
    public List<AppUserSummaryDto> list(@PathVariable("tenantId") Long tenantId) {
        return platformTenantUserService.listByTenant(tenantId);
    }

    @PostMapping
    public AppUserSummaryDto create(
            @PathVariable("tenantId") Long tenantId, @RequestBody PlatformUserCreateRequest body) {
        return platformTenantUserService.create(tenantId, body);
    }

    @PutMapping("/{userId}")
    public AppUserSummaryDto update(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("userId") Long userId,
            @RequestBody PlatformUserUpdateRequest body) {
        return platformTenantUserService.update(tenantId, userId, body);
    }
}

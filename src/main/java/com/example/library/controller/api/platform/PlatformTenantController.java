package com.example.library.controller.api.platform;

import com.example.library.dto.platform.TenantCreateRequest;
import com.example.library.dto.platform.TenantDto;
import com.example.library.dto.platform.TenantUpdateRequest;
import com.example.library.service.platform.PlatformTenantService;
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
@RequestMapping("/api/platform/tenants")
@PreAuthorize("hasAuthority(T(com.example.library.security.TenantRoles).AUTHORITY_PLATFORM_ADMIN)")
public class PlatformTenantController {

    private final PlatformTenantService platformTenantService;

    public PlatformTenantController(PlatformTenantService platformTenantService) {
        this.platformTenantService = platformTenantService;
    }

    @GetMapping
    public List<TenantDto> list() {
        return platformTenantService.listAll();
    }

    @GetMapping("/{id}")
    public TenantDto get(@PathVariable("id") Long id) {
        return platformTenantService.getById(id);
    }

    @PostMapping
    public TenantDto create(@RequestBody TenantCreateRequest body) {
        return platformTenantService.create(body);
    }

    @PutMapping("/{id}")
    public TenantDto update(@PathVariable("id") Long id, @RequestBody TenantUpdateRequest body) {
        return platformTenantService.update(id, body);
    }
}

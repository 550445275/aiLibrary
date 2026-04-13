package com.example.library.security;

import com.example.library.entity.AppUser;
import com.example.library.entity.Tenant;
import com.example.library.mapper.AppUserMapper;
import com.example.library.mapper.TenantMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryUserDetailsService implements UserDetailsService {

    private final AppUserMapper appUserMapper;
    private final TenantMapper tenantMapper;

    public LibraryUserDetailsService(AppUserMapper appUserMapper, TenantMapper tenantMapper) {
        this.appUserMapper = appUserMapper;
        this.tenantMapper = tenantMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantCode = TenantLoginContext.getTenantCode();
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new UsernameNotFoundException("缺少租户代码");
        }
        Tenant tenant = tenantMapper.selectByCode(tenantCode.trim());
        if (tenant == null) {
            throw new UsernameNotFoundException("租户不存在: " + tenantCode);
        }
        if (!tenant.isEnabled()) {
            throw new UsernameNotFoundException("租户已停用: " + tenantCode);
        }
        AppUser appUser = appUserMapper.selectByTenantAndUsername(tenant.getId(), username);
        if (appUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new LibraryUserDetails(
                tenant.getId(),
                tenant.getCode(),
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.isEnabled(),
                appUser.getRole(),
                appUser.isPlatformAdmin());
    }
}

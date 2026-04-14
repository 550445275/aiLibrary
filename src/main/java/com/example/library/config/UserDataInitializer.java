package com.example.library.config;

import com.example.library.entity.AppUser;
import com.example.library.entity.Tenant;
import com.example.library.security.TenantRoles;
import com.example.library.mapper.AppUserMapper;
import com.example.library.mapper.TenantMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 当默认租户下无任何用户时，创建 admin/admin。多租户下仅初始化「默认租户」的管理员，避免误为所有租户建号。
 */
@Component
@Order(1000)
public class UserDataInitializer implements CommandLineRunner {

    private final AppUserMapper appUserMapper;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(
            AppUserMapper appUserMapper, TenantMapper tenantMapper, PasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.tenantMapper = tenantMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Tenant tenant = tenantMapper.selectByCode("default");
        if (tenant == null) {
            return;
        }
        if (appUserMapper.countUsersByTenant(tenant.getId()) > 0) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setTenantId(tenant.getId());
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole(TenantRoles.ROLE_TENANT_ADMIN);
        admin.setEnabled(true);
        admin.setPlatformAdmin(true);
        appUserMapper.insert(admin);
    }
}

package com.example.library.security;

/**
 * 登录请求处理期间传递 tenantCode，供 {@link LibraryUserDetailsService#loadUserByUsername} 解析租户。
 */
public final class TenantLoginContext {

    private static final ThreadLocal<String> TENANT_CODE = new ThreadLocal<>();

    private TenantLoginContext() {
    }

    public static void setTenantCode(String tenantCode) {
        TENANT_CODE.set(tenantCode);
    }

    public static String getTenantCode() {
        return TENANT_CODE.get();
    }

    public static void clear() {
        TENANT_CODE.remove();
    }
}

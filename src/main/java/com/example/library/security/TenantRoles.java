package com.example.library.security;

/**
 * 租户侧 Spring Security 角色与库内 {@code app_users.role} 取值对齐。
 * <ul>
 *   <li>平台级：{@link #AUTHORITY_PLATFORM_ADMIN}，由 {@code platform_admin=1} 附加授予</li>
 *   <li>租户级：{@link #ROLE_TENANT_ADMIN}、{@link #ROLE_USER}</li>
 * </ul>
 */
public final class TenantRoles {

    /** 与 {@code app_users.platform_admin=1} 对应，附加到 {@link LibraryUserDetails} */
    public static final String AUTHORITY_PLATFORM_ADMIN = "ROLE_PLATFORM_ADMIN";

    /** 租户管理员（原 {@link #LEGACY_ROLE_ADMIN} 已迁移为该值） */
    public static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";

    /** 普通成员 */
    public static final String ROLE_USER = "ROLE_USER";

    /** 历史库中租户管理员曾用名，加载时规范化为 {@link #ROLE_TENANT_ADMIN} */
    public static final String LEGACY_ROLE_ADMIN = "ROLE_ADMIN";

    private TenantRoles() {}

    /** 将库内或请求中的角色名规范为当前权威字符串 */
    public static String normalize(String role) {
        if (role == null) {
            return ROLE_USER;
        }
        String t = role.trim();
        if (LEGACY_ROLE_ADMIN.equals(t)) {
            return ROLE_TENANT_ADMIN;
        }
        return t;
    }

    public static boolean isTenantAdminRole(String normalizedOrRaw) {
        String n = normalize(normalizedOrRaw);
        return ROLE_TENANT_ADMIN.equals(n);
    }

    public static boolean isMemberRole(String normalizedOrRaw) {
        return ROLE_USER.equals(normalize(normalizedOrRaw));
    }
}

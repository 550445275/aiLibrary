/**
 * 与后端 {@code TenantRoles} / {@code UserInfoResponse} 对齐。
 * 平台管理员：{@code platformAdmin === true}（对应 ROLE_PLATFORM_ADMIN）
 * 租户管理员：{@code tenantAdmin === true} 或角色为 ROLE_TENANT_ADMIN（兼容 ROLE_ADMIN）
 * 普通成员：{@code role === ROLE_USER}
 */
export const TENANT_ROLE = {
  TENANT_ADMIN: 'ROLE_TENANT_ADMIN',
  USER: 'ROLE_USER',
  /** 旧库或旧客户端可能仍传入 */
  LEGACY_ADMIN: 'ROLE_ADMIN',
}

export function isPlatformAdminUser(user) {
  return user?.platformAdmin === true
}

export function isTenantAdminUser(user) {
  if (!user) return false
  if (user.tenantAdmin === true) return true
  const r = user.role
  return r === TENANT_ROLE.TENANT_ADMIN || r === TENANT_ROLE.LEGACY_ADMIN
}

export function isTenantMemberUser(user) {
  return user?.role === TENANT_ROLE.USER
}

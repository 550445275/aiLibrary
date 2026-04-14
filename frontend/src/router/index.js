import { createRouter, createWebHistory } from 'vue-router'
import api from '../api'
import { canMutateBooks, isPlatformAdminUser, isTenantAdminUser } from '../auth/roles'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/books',
    name: 'books',
    component: () => import('../views/BookListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/books/new',
    name: 'book-new',
    component: () => import('../views/BookFormView.vue'),
    meta: { requiresAuth: true, requiresBookMutation: true },
  },
  {
    path: '/books/edit',
    name: 'book-edit',
    component: () => import('../views/BookFormView.vue'),
    meta: { requiresAuth: true, requiresBookMutation: true },
  },
  {
    path: '/admin/tenant-auth',
    name: 'admin-tenant-auth',
    component: () => import('../views/TenantAuthHubView.vue'),
    meta: {
      requiresAuth: true,
      requiresPlatformAdmin: true,
      roles: ['PLATFORM_ADMIN'],
    },
  },
  {
    path: '/admin/tenants',
    name: 'admin-tenants',
    component: () => import('../views/TenantListView.vue'),
    meta: {
      requiresAuth: true,
      requiresPlatformAdmin: true,
      roles: ['PLATFORM_ADMIN'],
    },
  },
  {
    path: '/admin/tenants/:tenantId/users',
    name: 'admin-tenant-users',
    component: () => import('../views/TenantUserAuthView.vue'),
    meta: {
      requiresAuth: true,
      requiresPlatformAdmin: true,
      roles: ['PLATFORM_ADMIN'],
    },
  },
  {
    path: '/',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth || to.meta.guestOnly) {
    try {
      const { data } = await api.get('/api/me')
      authStore.setUser(data)
      if (to.meta.guestOnly) {
        return { name: 'books' }
      }
      if (to.meta.requiresPlatformAdmin && !isPlatformAdminUser(data)) {
        return { name: 'books' }
      }
      if (to.meta.requiresTenantAdmin && !isTenantAdminUser(data)) {
        return { name: 'books' }
      }
      if (to.meta.requiresBookMutation && !canMutateBooks(data)) {
        return { name: 'books' }
      }
    } catch {
      authStore.clearUser()
      if (to.meta.requiresAuth) {
        return { name: 'login', query: { redirect: to.fullPath } }
      }
    }
  }

  return true
})

export default router

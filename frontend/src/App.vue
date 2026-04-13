<template>
  <div class="app-shell">
    <header v-if="authStore.user" class="top-bar">
      <span class="brand">图书管理</span>
      <nav v-if="authStore.user.platformAdmin" class="nav-admin">
        <router-link to="/admin/tenants">租户管理</router-link>
        <router-link to="/admin/tenant-auth">租户授权</router-link>
      </nav>
      <span class="user">
        {{ authStore.user.username }}
        <template v-if="authStore.user.tenantCode">（{{ authStore.user.tenantCode }}）</template>
      </span>
      <button type="button" class="link-btn" @click="logout">退出</button>
    </header>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import api from './api'

const router = useRouter()
const authStore = useAuthStore()

async function logout() {
  try {
    await api.post('/api/logout')
  } catch {
    /* ignore */
  }
  authStore.clearUser()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.nav-admin {
  display: flex;
  gap: 1rem;
  margin-right: 0.5rem;
}
.nav-admin a {
  font-size: 0.9rem;
}
</style>

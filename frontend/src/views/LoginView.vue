<template>
  <div class="card" style="max-width: 400px; margin: 2rem auto">
    <h1>登录</h1>
    <form class="form-grid" @submit.prevent="submit">
      <label>
        租户代码
        <input v-model.trim="tenantCode" type="text" autocomplete="organization" required placeholder="如 default" />
      </label>
      <label>
        用户名
        <input v-model.trim="username" type="text" autocomplete="username" required />
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>
      <div class="actions">
        <button type="submit" class="btn btn-primary" :disabled="loading">登录</button>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const tenantCode = ref('default')
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await api.post('/api/login', {
      tenantCode: tenantCode.value,
      username: username.value,
      password: password.value,
    })
    authStore.setUser(data)
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/books')
  } catch (e) {
    if (e.response?.status === 401) {
      error.value = '用户名或密码错误'
    } else {
      error.value = '登录失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

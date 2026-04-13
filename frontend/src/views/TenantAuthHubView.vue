<template>
  <div class="card" style="max-width: 480px">
    <h1>租户授权</h1>
    <p class="muted">选择租户后进入该租户下的用户账号与角色管理。</p>
    <div class="form-grid">
      <label>
        租户
        <select v-model="selectedId" :disabled="loading">
          <option value="" disabled>请选择</option>
          <option v-for="t in tenants" :key="t.id" :value="String(t.id)">
            {{ t.code }} — {{ t.name }}
          </option>
        </select>
      </label>
      <div class="actions">
        <button type="button" class="btn btn-primary" :disabled="!selectedId || loading" @click="go">
          进入用户管理
        </button>
        <router-link to="/admin/tenants" class="btn btn-secondary">租户列表</router-link>
        <router-link to="/books" class="btn btn-secondary">图书</router-link>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const tenants = ref([])
const selectedId = ref('')
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get('/api/platform/tenants')
    tenants.value = data
  } catch (e) {
    error.value = e.response?.status === 403 ? '无平台管理员权限' : '加载失败'
  } finally {
    loading.value = false
  }
}

function go() {
  if (!selectedId.value) return
  router.push({ name: 'admin-tenant-users', params: { tenantId: selectedId.value } })
}

onMounted(load)
</script>

<style scoped>
.muted {
  color: #6b7280;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
select {
  padding: 0.5rem 0.65rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
}
</style>

<template>
  <div class="card">
    <div class="toolbar">
      <h1 style="margin: 0">租户管理</h1>
      <div class="actions" style="margin: 0">
        <router-link to="/books" class="btn btn-secondary">图书</router-link>
        <button type="button" class="btn btn-primary" @click="openCreate">新建租户</button>
      </div>
    </div>
    <p v-if="error" class="error-msg">{{ error }}</p>
    <div v-else-if="loading">加载中…</div>
    <table v-else>
      <thead>
        <tr>
          <th>ID</th>
          <th>代码</th>
          <th>名称</th>
          <th>启用</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="t in tenants" :key="t.id">
          <td>{{ t.id }}</td>
          <td>{{ t.code }}</td>
          <td>{{ t.name }}</td>
          <td>{{ t.enabled ? '是' : '否' }}</td>
          <td>{{ formatTime(t.createdAt) }}</td>
          <td>
            <button type="button" class="link-btn" @click="openEdit(t)">编辑</button>
            <router-link :to="{ name: 'admin-tenant-users', params: { tenantId: t.id } }" class="link-btn" style="margin-left: 0.5rem">
              用户授权
            </router-link>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="dialogOpen" class="dialog-backdrop" @click.self="dialogOpen = false">
      <div class="dialog card">
        <h2>{{ editingId ? '编辑租户' : '新建租户' }}</h2>
        <form class="form-grid" @submit.prevent="saveTenant">
          <label v-if="!editingId">
            租户代码（小写字母开头；字母、数字、连字符、下划线）
            <input v-model.trim="form.code" type="text" required :disabled="!!editingId" />
          </label>
          <label>
            名称
            <input v-model.trim="form.name" type="text" required />
          </label>
          <label class="row-check">
            <input v-model="form.enabled" type="checkbox" />
            启用
          </label>
          <div class="actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">保存</button>
            <button type="button" class="btn btn-secondary" @click="dialogOpen = false">取消</button>
          </div>
          <p v-if="formError" class="error-msg">{{ formError }}</p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'

const tenants = ref([])
const loading = ref(true)
const error = ref('')
const dialogOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const form = reactive({ code: '', name: '', enabled: true })

function formatTime(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 19)
}

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

function openCreate() {
  editingId.value = null
  form.code = ''
  form.name = ''
  form.enabled = true
  formError.value = ''
  dialogOpen.value = true
}

function openEdit(t) {
  editingId.value = t.id
  form.code = t.code
  form.name = t.name
  form.enabled = t.enabled
  formError.value = ''
  dialogOpen.value = true
}

function errorText(e) {
  const d = e.response?.data
  if (d == null) return '保存失败'
  if (typeof d === 'string') return d
  return d.detail || d.message || d.error || '保存失败'
}

async function saveTenant() {
  formError.value = ''
  saving.value = true
  try {
    if (editingId.value != null && editingId.value !== '') {
      const id = Number(editingId.value)
      if (!Number.isSafeInteger(id) || id < 1) {
        formError.value = '租户 ID 无效，请关闭对话框后重新打开编辑'
        return
      }
      await api.put(`/api/platform/tenants/${id}`, {
        name: form.name,
        enabled: form.enabled,
      })
    } else {
      await api.post('/api/platform/tenants', {
        code: form.code.trim().toLowerCase(),
        name: form.name,
        enabled: form.enabled,
      })
    }
    dialogOpen.value = false
    await load()
  } catch (e) {
    formError.value = errorText(e)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}
.dialog {
  max-width: 420px;
  width: 100%;
}
.row-check {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}
</style>

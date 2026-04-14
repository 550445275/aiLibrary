<template>
  <div class="card">
    <div class="toolbar">
      <h1 style="margin: 0">租户用户授权</h1>
      <div class="actions" style="margin: 0">
        <router-link to="/admin/tenant-auth" class="btn btn-secondary">租户授权</router-link>
        <router-link to="/admin/tenants" class="btn btn-secondary">租户列表</router-link>
        <button type="button" class="btn btn-primary" @click="openCreate">新建用户</button>
      </div>
    </div>
    <p class="muted">租户 ID：{{ tenantId }}</p>
    <p v-if="error" class="error-msg">{{ error }}</p>
    <div v-else-if="loading">加载中…</div>
    <table v-else>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>角色</th>
          <th>启用</th>
          <th>平台管理员</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ formatRoleLabel(u.role) }}</td>
          <td>{{ u.enabled ? '是' : '否' }}</td>
          <td>{{ u.platformAdmin ? '是' : '否' }}</td>
          <td>
            <button type="button" class="link-btn" @click="openEdit(u)">编辑</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="dialogOpen" class="dialog-backdrop" @click.self="dialogOpen = false">
      <div class="dialog card">
        <h2>{{ createMode ? '新建用户' : '编辑用户' }}</h2>
        <form class="form-grid" @submit.prevent="saveUser">
          <template v-if="createMode">
            <label>
              用户名
              <input v-model.trim="createForm.username" type="text" required autocomplete="off" />
            </label>
            <label>
              密码
              <input v-model="createForm.password" type="password" required autocomplete="new-password" />
            </label>
            <label>
              角色
              <select v-model="createForm.role" required>
                <option :value="TENANT_ROLE.TENANT_ADMIN">租户管理员（ROLE_TENANT_ADMIN）</option>
                <option :value="TENANT_ROLE.USER">成员（ROLE_USER）</option>
              </select>
            </label>
            <label class="row-check">
              <input v-model="createForm.enabled" type="checkbox" />
              启用
            </label>
          </template>
          <template v-else>
            <label>
              角色
              <select v-model="editForm.role" required>
                <option :value="TENANT_ROLE.TENANT_ADMIN">租户管理员（ROLE_TENANT_ADMIN）</option>
                <option :value="TENANT_ROLE.USER">成员（ROLE_USER）</option>
              </select>
            </label>
            <label class="row-check">
              <input v-model="editForm.enabled" type="checkbox" />
              启用
            </label>
            <label class="row-check">
              <input v-model="editForm.platformAdmin" type="checkbox" />
              平台管理员
            </label>
          </template>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import { TENANT_ROLE } from '../auth/roles'

const route = useRoute()
const tenantId = computed(() => route.params.tenantId)

function formatRoleLabel(role) {
  if (role === TENANT_ROLE.LEGACY_ADMIN || role === TENANT_ROLE.TENANT_ADMIN) {
    return '租户管理员（ROLE_TENANT_ADMIN）'
  }
  if (role === TENANT_ROLE.USER) {
    return '成员（ROLE_USER）'
  }
  return role
}

function normalizeRoleForForm(role) {
  if (role === TENANT_ROLE.LEGACY_ADMIN) {
    return TENANT_ROLE.TENANT_ADMIN
  }
  return role
}

const users = ref([])
const loading = ref(true)
const error = ref('')
const dialogOpen = ref(false)
const createMode = ref(true)
const saving = ref(false)
const formError = ref('')
const editingUserId = ref(null)

const createForm = reactive({
  username: '',
  password: '',
  role: TENANT_ROLE.USER,
  enabled: true,
})

const editForm = reactive({
  role: TENANT_ROLE.USER,
  enabled: true,
  platformAdmin: false,
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get(`/api/platform/tenants/${tenantId.value}/users`)
    users.value = data
  } catch (e) {
    error.value = e.response?.status === 403 ? '无平台管理员权限' : '加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  createMode.value = true
  createForm.username = ''
  createForm.password = ''
  createForm.role = TENANT_ROLE.USER
  createForm.enabled = true
  formError.value = ''
  dialogOpen.value = true
}

function openEdit(u) {
  createMode.value = false
  editingUserId.value = u.id
  editForm.role = normalizeRoleForForm(u.role)
  editForm.enabled = u.enabled
  editForm.platformAdmin = u.platformAdmin
  formError.value = ''
  dialogOpen.value = true
}

async function saveUser() {
  formError.value = ''
  saving.value = true
  try {
    if (createMode.value) {
      await api.post(`/api/platform/tenants/${tenantId.value}/users`, { ...createForm })
    } else {
      await api.put(`/api/platform/tenants/${tenantId.value}/users/${editingUserId.value}`, {
        role: editForm.role,
        enabled: editForm.enabled,
        platformAdmin: editForm.platformAdmin,
      })
    }
    dialogOpen.value = false
    await load()
  } catch (e) {
    formError.value = e.response?.data?.message || e.response?.data?.error || '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.muted {
  color: #6b7280;
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}
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
select {
  padding: 0.5rem 0.65rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
}
</style>

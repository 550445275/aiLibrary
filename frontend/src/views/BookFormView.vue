<template>
  <div class="card">
    <h1>{{ isEdit ? '编辑图书' : '新建图书' }}</h1>
    <p v-if="loadError" class="error-msg">{{ loadError }}</p>
    <form v-else class="form-grid" @submit.prevent="submit">
      <label>
        书名 <span style="color: #dc2626">*</span>
        <input v-model.trim="form.title" type="text" required />
      </label>
      <label>
        作者
        <input v-model.trim="form.author" type="text" />
      </label>
      <label>
        ISBN
        <input v-model.trim="form.isbn" type="text" />
      </label>
      <label>
        出版年
        <input v-model.number="form.publishYear" type="number" min="0" />
      </label>
      <label>
        价格
        <input v-model="form.price" type="text" placeholder="如 39.90" />
      </label>
      <div class="actions">
        <button type="submit" class="btn btn-primary" :disabled="saving">保存</button>
        <router-link to="/books" class="btn btn-secondary">取消</router-link>
      </div>
      <p v-if="saveError" class="error-msg">{{ saveError }}</p>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => {
  const id = route.query.id
  return id != null && String(id).trim() !== ''
})

const form = reactive({
  title: '',
  author: '',
  isbn: '',
  publishYear: null,
  price: '',
})

const loadError = ref('')
const saveError = ref('')
const saving = ref(false)

function parsePrice() {
  const s = form.price
  if (s === '' || s == null) return null
  const n = Number(String(s).trim())
  return Number.isFinite(n) ? n : null
}

onMounted(async () => {
  if (!isEdit.value) return
  const id = route.query.id
  try {
    const { data } = await api.get(`/api/books/${id}`)
    form.title = data.title ?? ''
    form.author = data.author ?? ''
    form.isbn = data.isbn ?? ''
    form.publishYear = data.publishYear ?? null
    form.price = data.price != null ? String(data.price) : ''
  } catch {
    loadError.value = '加载图书失败'
  }
})

async function submit() {
  saveError.value = ''
  saving.value = true
  const body = {
    title: form.title,
    author: form.author || null,
    isbn: form.isbn || null,
    publishYear: form.publishYear,
    price: parsePrice(),
  }
  try {
    if (isEdit.value) {
      await api.put(`/api/books/${route.query.id}`, body)
    } else {
      await api.post('/api/books', body)
    }
    router.push('/books')
  } catch (e) {
    const msg = e.response?.data?.error
    saveError.value = typeof msg === 'string' ? msg : '保存失败'
  } finally {
    saving.value = false
  }
}
</script>

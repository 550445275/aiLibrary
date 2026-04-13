<template>
  <div class="card">
    <div class="toolbar">
      <h1 style="margin: 0">图书列表</h1>
      <router-link to="/books/new" class="btn btn-primary">新建</router-link>
    </div>
    <p v-if="loadError" class="error-msg">{{ loadError }}</p>
    <div v-else-if="loading">加载中…</div>
    <template v-else>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>书名</th>
            <th>作者</th>
            <th>ISBN</th>
            <th>出版年</th>
            <th>价格</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in page.content" :key="b.id">
            <td>{{ b.id }}</td>
            <td>{{ b.title }}</td>
            <td>{{ b.author ?? '—' }}</td>
            <td>{{ b.isbn ?? '—' }}</td>
            <td>{{ b.publishYear ?? '—' }}</td>
            <td>{{ b.price != null ? b.price : '—' }}</td>
            <td>
              <router-link :to="{ name: 'book-edit', query: { id: b.id } }">编辑</router-link>
              <button type="button" class="link-btn" style="margin-left: 0.5rem" @click="remove(b)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <label>
          每页
          <input
            v-model.number="size"
            type="number"
            min="1"
            max="50"
            style="width: 4rem; margin-left: 0.25rem"
          />
        </label>
        <span>第 {{ page.number + 1 }} / {{ totalPages }} 页，共 {{ page.totalElements }} 条</span>
        <button type="button" class="btn btn-secondary" :disabled="page.number <= 0" @click="prev">
          上一页
        </button>
        <button
          type="button"
          class="btn btn-secondary"
          :disabled="page.number >= totalPages - 1"
          @click="next"
        >
          下一页
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../api'

const loading = ref(true)
const loadError = ref('')
const page = ref({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 10,
})
const size = ref(10)

const totalPages = computed(() => Math.max(1, page.value.totalPages || 1))

async function fetchPage(p) {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await api.get('/api/books', {
      params: { page: p, size: size.value },
    })
    page.value = data
    size.value = data.size
  } catch (e) {
    loadError.value = e.response?.data?.error || '加载失败'
  } finally {
    loading.value = false
  }
}

function reloadFirst() {
  if (size.value < 1) size.value = 1
  if (size.value > 50) size.value = 50
  fetchPage(0)
}

function prev() {
  if (page.value.number > 0) fetchPage(page.value.number - 1)
}

function next() {
  if (page.value.number < page.value.totalPages - 1) fetchPage(page.value.number + 1)
}

async function remove(book) {
  if (!confirm(`确定删除《${book.title}》？`)) return
  try {
    await api.delete(`/api/books/${book.id}`)
    await fetchPage(page.value.number)
  } catch (e) {
    alert(e.response?.data?.message || e.response?.data?.error || '删除失败')
  }
}

let skipSizeWatch = true
onMounted(() => {
  fetchPage(0).then(() => {
    skipSizeWatch = false
  })
})

watch(size, () => {
  if (skipSizeWatch) return
  reloadFirst()
})
</script>

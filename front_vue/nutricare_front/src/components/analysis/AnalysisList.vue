<template>
  <section class="analysis-list">
    <h2>분석 기록</h2>

    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="items.length === 0" class="alert alert-light text-center" role="alert">
      분석 기록이 없습니다.
    </div>

    <template v-else>
      <div class="table-wrap">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col">#</th>
              <th scope="col">분석 날짜</th>
              <th scope="col">사진이름</th>
              <th scope="col">분석명</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(item, index) in pagedItems"
              :key="item.id"
              @click="goDetail(item.id)"
            >
              <td>{{ (page - 1) * pageSize + index + 1 }}</td>
              <td>{{ item.date }}</td>
              <td>{{ item.photoName }}</td>
              <td>{{ item.title }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-controls">
        <button class="btn btn-sm btn-outline-secondary" :disabled="page === 1" @click="page--">
          ‹
        </button>
        <span v-for="p in totalPages" :key="p">
          <button
            class="btn btn-sm"
            :class="{ 'btn-primary': p === page, 'btn-outline-secondary': p !== page }"
            @click="page = p"
          >
            {{ p }}
          </button>
        </span>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page === totalPages" @click="page++">
          ›
        </button>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'

const router = useRouter()
const store = useAnalysisStore()

const isLoading = ref(false)

onMounted(async () => {
  isLoading.value = true
  try {
    await store.fetchUserPhotos()
  } finally {
    isLoading.value = false
  }
})

function extractPhotoName(url) {
  if (!url || !url.includes('_')) {
    return 'N/A'
  }
  return url.split('_').pop()
}

const items = computed(() =>
  [...store.user_photos]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .map((p) => ({
      id: p.photoId,
      date: new Date(p.createdAt).toLocaleString(),
      photoName: extractPhotoName(p.photoUrl),
      title: p.analysisResult?.diagnosisName || '분석 대기중',
    }))
)

const page = ref(1)
const pageSize = 10
const totalPages = computed(() => Math.ceil(items.value.length / pageSize) || 1)
const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize
  return items.value.slice(start, start + pageSize)
})

function goDetail(id) {
  router.push({ name: 'analysisDetail', params: { photoId: id } }).catch(() => {})
}
</script>

<style scoped>
.analysis-list {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 16px;
}

h2 {
  margin-bottom: 2rem;
  text-align: center;
}

.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 0.25rem;
  background: #fff;
}

.table {
  margin-bottom: 0;
}

.table tbody tr {
  cursor: pointer;
}

.pagination-controls {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
</style>

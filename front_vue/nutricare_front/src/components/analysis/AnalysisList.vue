<template>
  <section class="analysis-list">
    <table>
      <thead>
        <tr>
          <th>순번</th>
          <th>분석 날짜</th>
          <th>사진이름</th>
          <th>분석명</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(item, index) in pagedItems"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <td>{{ (page - 1) * pageSize + index + 1 }}</td>
          <td class="with-icon">📅 {{ item.date }}</td>
          <td>{{ item.photoName }}</td>
          <td>{{ item.title }}</td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button type="button" :disabled="page===1" @click="page--">‹</button>
      <button
        v-for="p in totalPages"
        :key="p"
        :class="{ active: p === page }"
        type="button"
        @click="page = p"
      >
        {{ p }}
      </button>
      <button type="button" :disabled="page===totalPages" @click="page++">›</button>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'

const router = useRouter()
const store = useAnalysisStore()

onMounted(() => {
  store.fetchUserPhotos()
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
const pageSize = 7
const totalPages = computed(() => Math.ceil(items.value.length / pageSize))
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
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 16px 48px;
  background: #f8f5eb;
  box-sizing: border-box;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

th, td {
  border: 1px solid #9f9f9f;
  padding: 12px 14px;
  text-align: left;
}

tbody tr {
  cursor: pointer;
}

tbody tr:hover {
  background: #f1f1f1;
}

.with-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.pagination button {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 6px 8px;
}

.pagination button.active {
  font-weight: 700;
  text-decoration: underline;
}

.pagination button:disabled {
  color: #b0b0b0;
  cursor: not-allowed;
}
</style>

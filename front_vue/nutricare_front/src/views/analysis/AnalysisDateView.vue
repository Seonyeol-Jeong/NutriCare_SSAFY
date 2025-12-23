<template>
  <div class="analysis-date-container">
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <i class="bi bi-arrow-left"></i>
      </button>
      <h2 class="page-title">{{ dateTitle }} 분석 기록</h2>
    </div>

    <div v-if="isLoading" class="loading-state">
      로딩 중...
    </div>

    <div v-else-if="dayItems.length === 0" class="empty-state">
      <p>해당 날짜에 분석 기록이 없습니다.</p>
    </div>

    <div v-else class="gallery-grid">
      <div 
        v-for="item in dayItems" 
        :key="item.id" 
        class="gallery-item"
        @click="openDetail(item.id)"
      >
        <img :src="item.thumbnail" alt="분석 이미지" />
        <div class="item-overlay">
          <span class="time-badge">{{ formatTime(item.date) }}</span>
        </div>
      </div>
    </div>

    <!-- 상세 보기 모달 -->
    <AnalysisDetailModal 
      v-if="selectedPhotoId" 
      :photoId="selectedPhotoId" 
      @close="closeDetail" 
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import AnalysisDetailModal from '@/components/analysis/AnalysisDetailModal.vue'

const route = useRoute()
const router = useRouter()
const store = useAnalysisStore()

const isLoading = ref(false)
const selectedPhotoId = ref(null)

const targetDateStr = route.params.date // YYYY-MM-DD

const dateTitle = computed(() => {
  if (!targetDateStr) return ''
  const [y, m, d] = targetDateStr.split('-')
  return `${y}년 ${m}월 ${d}일`
})

const dayItems = computed(() => {
  // store의 user_photos에서 해당 날짜 필터링
  // store가 비어있을 수 있으므로 fetch 필요할 수 있음
  const target = new Date(targetDateStr).toDateString()
  
  return store.user_photos
    .filter(p => new Date(p.createdAt).toDateString() === target)
    .map(p => ({
      id: p.photoId,
      date: new Date(p.createdAt),
      thumbnail: p.photoUrl
    }))
    .sort((a, b) => b.date - a.date) // 최신순
})

function formatTime(date) {
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

function openDetail(id) {
  selectedPhotoId.value = id
}

function closeDetail() {
  selectedPhotoId.value = null
}

onMounted(async () => {
  if (store.user_photos.length === 0) {
    isLoading.value = true
    await store.fetchUserPhotos()
    isLoading.value = false
  }
})
</script>

<style scoped>
.analysis-date-container {
  max-width: 1000px;
  width: 100%;
  margin: 40px auto;
  padding: 30px;
  background-color: white;
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #333;
  padding: 4px;
  border-radius: 50%;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:hover {
  background-color: #f5f5f5;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #333;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
}

.gallery-item {
  aspect-ratio: 1;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 1px solid #eee;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.gallery-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 12px;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
  color: white;
  display: flex;
  justify-content: flex-end;
}

.time-badge {
  font-size: 12px;
  font-weight: 500;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px;
  color: #888;
  font-size: 16px;
}
</style>
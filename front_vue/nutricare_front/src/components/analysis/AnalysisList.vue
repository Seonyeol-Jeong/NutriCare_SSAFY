<template>
  <section class="analysis-container">
    <header class="header-section">
      <h2 class="title">나의 분석 리포트</h2>
      <p class="subtitle">AI가 분석한 피부/식단 기록을 확인해보세요.</p>
    </header>

    <div v-if="isLoading" class="loading-area">
      <div class="spinner"></div>
    </div>

    <div v-else-if="items.length === 0" class="empty-state">
      <div class="empty-icon">📂</div>
      <p>아직 분석 기록이 없습니다.</p>
      <button class="upload-btn" @click="goToUpload">첫 분석 시작하기</button>
    </div>

    <template v-else>
      <div class="gallery-grid">
        <article
          v-for="item in pagedItems"
          :key="item.id"
          class="analysis-card"
          @click="goDetail(item.id)"
        >
          <div class="image-wrapper">
            <img :src="item.thumbnail" alt="분석 사진" loading="lazy" />
            <div class="overlay">
              <span class="view-btn">상세보기</span>
            </div>
          </div>
          
          <div class="card-content">
            <div class="card-header">
              <span 
                class="status-badge" 
                :class="item.isAnalyzed ? 'done' : 'pending'"
              >
                {{ item.isAnalyzed ? '분석 완료' : '대기중' }}
              </span>
              <span class="date">{{ formatDate(item.date) }}</span>
            </div>
            
            <h3 class="diagnosis-title">{{ item.title }}</h3>
            <p class="filename">{{ item.photoName }}</p>
          </div>
        </article>
      </div>

      <div class="pagination">
        <button class="page-btn nav-btn" :disabled="page === 1" @click="page--">
          &lt;
        </button>
        <button
          v-for="p in totalPages"
          :key="p"
          class="page-btn"
          :class="{ active: p === page }"
          @click="page = p"
        >
          {{ p }}
        </button>
        <button class="page-btn nav-btn" :disabled="page === totalPages" @click="page++">
          &gt;
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
  if (!url) return 'Unknown';
  // URL에서 마지막 파일명 추출
  const filename = url.split('/').pop();
  
  // UUID 패턴(36자) + 언더바(_) 제거 시도
  // 예: "UUID_실제파일명.jpg" 형태라고 가정
  if (filename.includes('_')) {
    // 첫 번째 언더바 이후의 모든 문자열 반환 (UUID에 언더바가 없을 경우 안전)
    // 하지만 UUID에 언더바가 포함될 확률은 낮으므로 간단히 처리
    return filename.substring(filename.indexOf('_') + 1);
  }
  return filename;
}

const items = computed(() =>
  [...store.user_photos]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .map((p) => {
      const diagnosisName = p.analysisResult?.diagnosisName;
      return {
        id: p.photoId,
        date: p.createdAt, // 원본 날짜 객체 또는 문자열 유지
        photoName: extractPhotoName(p.photoUrl),
        title: diagnosisName || '분석 결과 없음',
        isAnalyzed: !!diagnosisName, // 진단명이 있으면 분석 완료로 간주
        thumbnail: p.photoUrl
      }
    })
)

const page = ref(1)
const pageSize = 9 // 그리드 뷰에 맞게 조정 (3x3)
const totalPages = computed(() => Math.ceil(items.value.length / pageSize) || 1)
const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize
  return items.value.slice(start, start + pageSize)
})

function formatDate(dateStr) {
  const date = new Date(dateStr);
  return date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
}

function goDetail(id) {
  router.push({ name: 'analysisDetail', params: { photoId: id } }).catch(() => {})
}

function goToUpload() {
  router.push({ name: 'analysisUpload' }).catch(() => {})
}
</script>

<style scoped>
.analysis-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 800;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 16px;
}

/* Gallery Grid */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
}

.analysis-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
}

.analysis-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.1);
  border-color: #eaddff;
}

.image-wrapper {
  width: 100%;
  height: 200px; /* 고정 높이 */
  position: relative;
  background-color: #f0f0f0;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.analysis-card:hover .image-wrapper img {
  transform: scale(1.05);
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.analysis-card:hover .overlay {
  opacity: 1;
}

.view-btn {
  color: white;
  border: 1px solid white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
}

.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-badge {
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 700;
}

.status-badge.done {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-badge.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.date {
  font-size: 13px;
  color: #999;
}

.diagnosis-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  
  /* 긴 제목 말줄임 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.filename {
  font-size: 13px;
  color: #888;
  margin: 0;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #666;
  font-size: 16px;
  margin-bottom: 24px;
}

.upload-btn {
  background-color: #6b55c7;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.upload-btn:hover {
  background-color: #5a45b0;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 40px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: white;
  border-radius: 8px;
  color: #555;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.page-btn:hover:not(:disabled) {
  background: #f3f0ff;
  color: #6b55c7;
}

.page-btn.active {
  background: #6b55c7;
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading */
.loading-area {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #6b55c7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

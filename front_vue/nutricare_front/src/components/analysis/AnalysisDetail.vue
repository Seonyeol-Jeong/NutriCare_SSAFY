<template>
  <div v-if="pageLoading" class="loading-container">
    <p>분석 정보를 불러오는 중입니다...</p>
  </div>
  <section v-else class="detail-container">
    <!-- 상단 정보: 사진 및 기본 분석 -->
    <header class="detail-header">
      <div class="photo-container">
        <img v-if="user_photo?.photoUrl" :src="user_photo.photoUrl" alt="분석 사진" class="user-photo-img" />
        <div v-else class="photo-placeholder">사진 없음</div>
        <p v-if="displayFileName" class="photo-filename">{{ displayFileName }}</p>
      </div>
      <div class="info-stack">
        <h2 class="diagnosis-title">{{ user_analysis_result?.diagnosisName || user_analysis_result?.diagnosis_name || '분석 결과 없음' }}</h2>
        <div class="info-field">
          <span class="info-label">분석 날짜</span>
          <span>{{ user_photo?.createdAt ? new Date(user_photo.createdAt).toLocaleString() : '정보 없음' }}</span>
        </div>
        <p class="disclaimer">* 이 결과는 참고용이며, 정확한 진단은 전문의와 상담하세요.</p>

        <!-- 상세 확률 정보 -->
        <div class="probabilities-chart" v-if="hasProbabilities">
          <h4>상세 분석</h4>
          <div v-for="item in sortedProbabilities.slice(0, 3)" :key="item.key" class="prob-row">
            <span class="prob-name">{{ item.label }}</span>
            <div class="prob-bar-bg">
              <div class="prob-bar-fill" :style="{ width: `${item.value * 100}%`, backgroundColor: item.color }"></div>
            </div>
            <span class="prob-percent">{{ (item.value * 100).toFixed(1) }}%</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 추천 식단 목록 또는 생성 버튼 -->
    <main class="recommendations-list">
      <h3>AI 추천 식단</h3>
      <!-- 로딩 중 -->
      <div v-if="diet_loading" class="loading-container">
        <div class="loading-content">
          <!-- 스피너 (버퍼 UI) -->
          <div class="spinner-buffer">
            <div class="spinner-track"></div>
            <div class="spinner-fill"></div>
          </div>
          
          <!-- 메시지 슬라이드 애니메이션 -->
          <div class="message-wrapper">
             <Transition name="slide-up" mode="out-in">
                <p :key="loadingMessage" class="loading-text">{{ loadingMessage }}</p>
             </Transition>
          </div>
        </div>
      </div>

      <!-- 추천 생성 버튼 (목록이 비어있을 때) -->
      <div v-else-if="isRecommendationEmpty" class="create-recommendation-prompt">
        <p>이 분석 결과에 대한 맞춤 식단 추천을 받아보세요.</p>
        <button type="button" @click.stop="handleCreateRecommendation" class="create-button">
          ✨ 식단 추천 생성하기
        </button>
        <p v-if="diet_error" class="error-msg">{{ diet_error }}</p>
      </div>

      <!-- 추천 목록 (목록이 있을 때) -->
      <div v-else>
        <div v-for="rec in diet_recommendations" :key="rec.resultId" class="recommendation-card">
          <div class="card-header">
            <h4>{{ rec.menuName }}</h4>
            <span class="calories">{{ rec.calories }} kcal</span>
          </div>
          <p class="description">{{ rec.description }}</p>
          <div v-if="rec.notes" class="notes">
            <strong>📝 Tip:</strong> {{ rec.notes }}
          </div>
          
          <!-- 유튜브 영상 임베드 영역 -->
          <div v-if="rec.embedUrl" class="video-container">
            <div class="video-wrapper">
              <iframe 
                :src="rec.embedUrl" 
                title="YouTube video player" 
                frameborder="0" 
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                allowfullscreen
              ></iframe>
            </div>
            <div class="video-info">
              <h5 class="video-title">{{ rec.videoTitle }}</h5>
              <div class="video-stats">
                <span>👁️ 조회수 {{ formatCount(rec.viewCount) }}회</span>
                <span>👍 좋아요 {{ formatCount(rec.likeCount) }}개</span>
              </div>
            </div>
          </div>
          <!-- 영상 로딩 중 -->
          <div v-else-if="rec.menuName && youtubeLoading" class="video-loading">
            <p>🎥 추천 레시피 영상을 찾는 중...</p>
          </div>
        </div>
        <p class="source-credit">식품의약품안전처에서 제공한 음식DB를 가공하여 활용하였습니다.</p>
      </div>
    </main>

    <footer class="detail-footer">
      <button type="button" @click="goList">목록으로</button>
    </footer>
  </section>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAnalysisStore } from '@/stores/analysis'
import { useUserStore } from '@/stores/user'
import { searchRecipeVideo } from '@/api/youtube'

const router = useRouter()
const route = useRoute()

// 스토어 인스턴스 생성
const analysisStore = useAnalysisStore()
const userStore = useUserStore()

// 스토어에서 상태를 반응형으로 가져오기
const { 
  user_photo, 
  user_analysis_result, 
  diet_recommendations, 
  diet_loading, 
  diet_error 
} = storeToRefs(analysisStore)

// 확률 매핑 정보
const probabilityMap = [
  { key: 'prob_gunsun', label: '건선', color: '#FF6B6B' },
  { key: 'prob_atopy', label: '아토피', color: '#4ECDC4' },
  { key: 'prob_acne', label: '여드름', color: '#FFE66D' },
  { key: 'prob_rosacea', label: '주사', color: '#FF9F43' },
  { key: 'prob_seborr', label: '지루성 피부염', color: '#1A535C' },
  { key: 'prob_normal', label: '정상', color: '#6ab04c' },
]

const hasProbabilities = computed(() => {
  const res = user_analysis_result.value
  return res && (res.prob_gunsun !== undefined || res.prob_rosacea !== undefined)
})

const sortedProbabilities = computed(() => {
  const res = user_analysis_result.value
  if (!res) return []
  return probabilityMap.map(item => {
    // snake_case, camelCase 모두 대응
    const val = res[item.key] !== undefined ? res[item.key] : (res[item.key.replace('prob_', 'prob')] || 0)
    return { ...item, value: Number(val) }
  }).sort((a, b) => b.value - a.value)
})

const pageLoading = ref(true) // 초기 페이지 로딩 상태
const photoId = route.params.photoId

// 식단 추천이 비어있는지 확인하는 computed
const isRecommendationEmpty = computed(() => diet_recommendations.value.length === 0)

// URL에서 파일명을 추출하는 computed
const displayFileName = computed(() => {
  if (!user_photo.value?.photoUrl) return ''
  // 1. URL의 마지막 세그먼트 추출
  const segments = user_photo.value.photoUrl.split('/')
  const lastSegment = segments[segments.length - 1]
  // 2. UUID(36자 + 언더바) 제거 시도 (예: 1c6f5ec3-6946-4abf-bc5d-dbd1e9881a85_파일명.webp)
  // 언더바(_) 기준으로 나누어 첫 번째 파트가 UUID 형식인 경우 뒷부분 사용
  if (lastSegment.includes('_')) {
    return lastSegment.substring(lastSegment.indexOf('_') + 1)
  }
  return lastSegment
})

// 유튜브 정보 로딩 상태
const youtubeLoading = ref(false);

// 식단 목록이 변경되면 유튜브 정보를 가져옵니다.
watch(diet_recommendations, async (newVal) => {
  if (newVal && newVal.length > 0) {
    await fetchYoutubeInfoForList();
  }
});

// 로딩 메시지 로테이션 로직
const loadingMessage = ref('AI가 맞춤 식단을 생성하는 중입니다...')
let loadingInterval = null

const loadingMessages = [
  '피부 상태에 맞는 영양소를 분석하고 있습니다...',
  '식품의약품안전처 DB에서 최적의 식재료를 검색 중입니다...',
  '건강하고 맛있는 레시피를 조합하고 있습니다...',
  '유튜브에서 관련 레시피 영상을 찾고 있습니다...',
  '거의 다 되었습니다! 잠시만 기다려주세요...'
]

watch(diet_loading, (newVal) => {
  if (newVal) {
    let index = 0
    loadingMessage.value = loadingMessages[0]
    loadingInterval = setInterval(() => {
      index = (index + 1) % loadingMessages.length
      loadingMessage.value = loadingMessages[index]
    }, 4000) // 4초마다 변경
  } else {
    if (loadingInterval) {
      clearInterval(loadingInterval)
      loadingInterval = null
    }
  }
})

async function fetchYoutubeInfoForList() {
  if (youtubeLoading.value) return;
  youtubeLoading.value = true;

  // 이미 영상 정보가 있는 항목은 건너뛰고, 없는 항목만 검색
  const promises = diet_recommendations.value.map(async (rec) => {
    if ((!rec.embedUrl) && rec.menuName) {
      const videoInfo = await searchRecipeVideo(rec.menuName);
      if (videoInfo) {
        // 반응형 상태 업데이트
        rec.embedUrl = videoInfo.embedUrl;
        rec.videoTitle = videoInfo.title;
        rec.viewCount = videoInfo.viewCount;
        rec.likeCount = videoInfo.likeCount;
      }
    }
  });

  await Promise.all(promises);
  youtubeLoading.value = false;
}

onMounted(async () => {
  if (!photoId) {
    alert('잘못된 접근입니다.')
    router.replace({ name: 'analysisList', params: { userId: userStore.userId || 'me' } })
    return
  }

  pageLoading.value = true
  analysisStore.diet_recommendations = []
  analysisStore.diet_error = null

  try {
    // 1. 사진 정보와 분석 결과를 먼저 가져옵니다.
    await Promise.all([
      analysisStore.fetchPhoto(photoId),
      analysisStore.fetchAnalysisResultByPhotoId(photoId)
    ])

    const analysisId = user_analysis_result.value?.analysisId
    if (analysisId) {
      // 2. analysisId로 기존 추천이 있는지 확인합니다.
      const recommendationHeader = await analysisStore.fetchRecommendationHeaderByAnalysisId(analysisId)

      // 3. 기존 추천이 있으면 (recId가 있으면) 메뉴 목록을 가져옵니다.
      if (recommendationHeader && recommendationHeader.recId) {
        await analysisStore.fetchDietRecommendationById(recommendationHeader.recId)
      }
    }
  } catch (e) {
    console.error("초기 정보를 불러오는 중 오류 발생:", e)
    alert("상세 정보를 가져오는 데 실패했습니다.")
  } finally {
    pageLoading.value = false
  }
})

async function handleCreateRecommendation() {
  const analysisId = user_analysis_result.value?.analysisId
  console.log("handleCreateRecommendation called. analysisId:", analysisId);
  
  if (!analysisId) {
    alert("분석 ID가 없어 추천을 생성할 수 없습니다.")
    return
  }
  
  try {
    await analysisStore.createAndFetchDietRecommendation({
      analysisId,
      memo: "식단 추천 생성 요청"
    })
  } catch (err) {
    console.error("[AnalysisDetail] 실행 중 에러 발생:", err);
    alert("오류가 발생했습니다: " + err.message);
  }
}


function goList() {
  const userId = userStore.userId || 'me' // 로그인 안했을 경우 대비
  router.push({ name: 'analysisList', params: { userId } }).catch(() => {})
}

function formatCount(num) {
  if (!num) return 0
  if (num >= 10000) return `${(num / 10000).toFixed(1)}만`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}천`
  return num
}
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 250px;
  color: #666;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* 버퍼형 스피너 UI */
.spinner-buffer {
  position: relative;
  width: 60px;
  height: 60px;
}

.spinner-track {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 4px solid #e0e0e0;
  border-radius: 50%;
}

.spinner-fill {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-top-color: #6b55c7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 메시지 래퍼 (공간 확보) */
.message-wrapper {
  height: 24px; /* 텍스트 높이 고정하여 레이아웃 이동 방지 */
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-text {
  font-size: 16px;
  font-weight: 500;
  color: #6b55c7;
  margin: 0;
  text-align: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 슬라이드 업 & 페이드 트랜지션 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.5s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.detail-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  background-color: #f8f5eb;
}

.detail-header {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

.photo-container {
  width: 200px;
  border-radius: 12px;
  overflow: visible; /* 파일명을 보여주기 위해 visible로 변경 */
  flex-shrink: 0;
}

.user-photo-img {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.photo-filename {
  margin-top: 8px;
  font-size: 12px;
  color: #888;
  text-align: center;
  word-break: break-all;
  line-height: 1.2;
}

.photo-placeholder {
  width: 200px;
  height: 200px;
  display: grid;
  place-items: center;
  background-color: #e0e0e0;
  color: #888;
  border-radius: 12px;
}

.info-stack {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.diagnosis-title {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.info-field {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  font-weight: 600;
  background-color: #eaddff;
  padding: 4px 8px;
  border-radius: 4px;
}

.disclaimer {
  font-size: 13px;
  color: #e67e22;
  font-weight: 600;
  margin: 4px 0;
}

.probabilities-chart {
  margin-top: 16px;
  background: rgba(255, 255, 255, 0.5);
  padding: 12px;
  border-radius: 8px;
  width: 100%;
}

.probabilities-chart h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
}

.prob-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}

.prob-name {
  width: 100px; /* 60px에서 늘림 */
  font-weight: 500;
  color: #333;
  white-space: nowrap; /* 글자 줄바꿈 방지 */
}

.prob-bar-bg {
  flex: 1;
  height: 6px;
  background-color: #ddd;
  border-radius: 3px;
  overflow: hidden;
}

.prob-bar-fill {
  height: 100%;
  border-radius: 3px;
}

.prob-percent {
  width: 40px;
  text-align: right;
  color: #555;
  font-weight: 600;
}

.recommendations-list h3 {
  font-size: 20px;
  margin-bottom: 16px;
  border-bottom: 2px solid #6b55c7;
  padding-bottom: 8px;
}

/* 추천 생성 버튼 영역 */
.create-recommendation-prompt {
  text-align: center;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  border: 2px dashed #6b55c7;
  position: relative;
  z-index: 10;
}

.create-recommendation-prompt p {
  font-size: 16px;
  margin-bottom: 20px;
}

.create-button {
  background: linear-gradient(45deg, #6b55c7, #8e44ad);
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 50px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}


.recommendation-card {
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-header h4 {
  margin: 0;
  font-size: 18px;
  color: #6b55c7;
}

.calories {
  font-size: 14px;
  font-weight: 600;
  color: #e67e22;
}

.description {
  font-size: 15px;
  color: #555;
  margin-bottom: 12px;
}

.notes {
  background-color: #f1f8e9;
  border-left: 4px solid #8bc34a;
  padding: 10px;
  font-size: 14px;
  margin-bottom: 12px;
  border-radius: 0 4px 4px 0;
}

.recipe-link {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #f5f5f5;
  padding: 8px;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: background-color 0.2s;
}

.recipe-link:hover {
  background-color: #e0e0e0;
}

.recipe-thumbnail {
  width: 120px;
  height: 67px;
  object-fit: cover;
  border-radius: 4px;
}

.recipe-link-text {
  flex-grow: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.arrow {
  font-size: 20px;
}

/* 비디오 스타일 */
.video-container {
  margin-top: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
}

.video-wrapper {
  position: relative;
  padding-bottom: 56.25%; /* 16:9 비율 */
  height: 0;
  overflow: hidden;
}

.video-wrapper iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.video-info {
  padding: 12px;
}

.video-title {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  
  /* 두 줄까지만 표시 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-stats {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #666;
}

.video-loading {
  margin-top: 12px;
  text-align: center;
  color: #888;
  font-size: 14px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.source-credit {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: 8px;
  font-style: italic;
}

.no-recommendations {
  text-align: center;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
}

.error-msg {
  color: #e74c3c;
  margin-top: 10px;
}

.detail-footer {
  margin-top: 32px;
  text-align: right;
}

.detail-footer button {
  padding: 10px 20px;
  background-color: #6b55c7;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
}

@media (max-width: 600px) {
  .detail-header {
    flex-direction: column;
  }
  .photo-container {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }
}
</style>
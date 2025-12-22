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
      </div>
      <div class="info-stack">
        <h2 class="diagnosis-title">{{ user_analysis_result?.diagnosisName || '분석 결과 없음' }}</h2>
        <div class="info-field">
          <span class="info-label">분석 날짜</span>
          <span>{{ user_photo?.createdAt ? new Date(user_photo.createdAt).toLocaleString() : '정보 없음' }}</span>
        </div>
      </div>
    </header>

    <!-- 추천 식단 목록 또는 생성 버튼 -->
    <main class="recommendations-list">
      <h3>AI 추천 식단</h3>
      <!-- 로딩 중 -->
      <div v-if="diet_loading" class="loading-container">
        <p>AI가 맞춤 식단을 생성하는 중입니다...</p>
      </div>

      <!-- 추천 생성 버튼 (목록이 비어있을 때) -->
      <div v-else-if="isRecommendationEmpty" class="create-recommendation-prompt">
        <p>이 분석 결과에 대한 맞춤 식단 추천을 받아보세요.</p>
        <button @click="handleCreateRecommendation" class="create-button">
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
          
          <a v-if="rec.recipeUrl" :href="rec.recipeUrl" target="_blank" rel="noopener noreferrer" class="recipe-link">
            <img v-if="rec.thumbnailUrl" :src="rec.thumbnailUrl" alt="레시피 썸네일" class="recipe-thumbnail"/>
            <div class="recipe-link-text">
              <span>레시피 영상 보러가기</span>
              <span class="arrow">→</span>
            </div>
          </a>
        </div>
      </div>
    </main>

    <footer class="detail-footer">
      <button type="button" @click="goList">목록으로</button>
    </footer>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAnalysisStore } from '@/stores/analysis'
import { useUserStore } from '@/stores/user'

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

const pageLoading = ref(true) // 초기 페이지 로딩 상태
const photoId = route.params.photoId

// 식단 추천이 비어있는지 확인하는 computed
const isRecommendationEmpty = computed(() => diet_recommendations.value.length === 0)

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
  console.log("[AnalysisDetail] 버튼 클릭됨. analysisId:", analysisId);
  
  if (!analysisId) {
    console.error("[AnalysisDetail] analysisId가 없습니다!", user_analysis_result.value);
    alert("분석 ID가 없어 추천을 생성할 수 없습니다.")
    return
  }
  
  console.log("[AnalysisDetail] 스토어 함수 호출 준비...");
  console.log("[AnalysisDetail] analysisStore:", analysisStore);
  
  try {
    if (typeof analysisStore.createAndFetchDietRecommendation !== 'function') {
       throw new Error("createAndFetchDietRecommendation 함수가 스토어에 없습니다.");
    }

    console.log("[AnalysisDetail] 스토어의 createAndFetchDietRecommendation 호출 시도...");
    await analysisStore.createAndFetchDietRecommendation({
      analysisId,
      memo: "식단 추천 생성 요청"
    })
    console.log("[AnalysisDetail] 스토어 함수 실행 완료");
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
  min-height: 200px;
  font-size: 16px;
  color: #666;
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
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.user-photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background-color: #e0e0e0;
  color: #888;
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
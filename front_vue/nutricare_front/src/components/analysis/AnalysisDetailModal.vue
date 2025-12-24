<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-window">
      <button class="close-btn" @click="$emit('close')">
        <i class="bi bi-x-lg"></i>
      </button>
      
      <div class="modal-content-scroll">
        <!-- 기존 AnalysisDetail.vue 내용 이식 -->
        <div v-if="loading" class="loading-container">
          <div class="spinner-border text-primary" role="status"></div>
          <p>분석 정보를 불러오는 중입니다...</p>
        </div>
        
        <section v-else class="detail-container">
          <!-- 상단 정보: 사진 및 기본 분석 -->
          <header class="detail-header">
            <div class="photo-container">
              <img v-if="user_photo?.photoUrl" :src="user_photo.photoUrl" alt="분석 사진" class="user-photo-img" />
              <div v-else class="photo-placeholder">사진 없음</div>
              
              <!-- 근처 피부과 찾기 버튼 -->
              <button @click="goToDermatologist" class="find-hospital-btn">
                <i class="bi bi-geo-alt-fill"></i> 근처 피부과 찾기
              </button>
            </div>
            <div class="info-stack">
              <h2 class="diagnosis-title">{{ user_analysis_result?.diagnosisName || user_analysis_result?.diagnosis_name || '분석 결과 없음' }}</h2>
              <div class="info-field">
                <span class="info-label">분석 날짜</span>
                <span>{{ user_photo?.createdAt ? new Date(user_photo.createdAt).toLocaleString() : '정보 없음' }}</span>
              </div>

              <!-- 상세 확률 정보 -->
              <div class="probabilities-chart" v-if="hasProbabilities">
                <h4>상세 분석</h4>
                <div v-for="item in sortedProbabilities.slice(0, 4)" :key="item.key" class="prob-row">
                  <span class="prob-name">{{ item.label }}</span>
                  <div class="prob-bar-bg">
                    <div class="prob-bar-fill" :style="{ width: `${item.value * 100}%`, backgroundColor: item.color }"></div>
                  </div>
                  <span class="prob-percent">{{ (item.value * 100).toFixed(1) }}%</span>
                </div>
              </div>
            </div>
          </header>

          <!-- 질환 정보 가이드 (해당 질환 데이터가 있을 경우 표시) -->
          <section class="disease-guide" v-if="currentDiseaseData">
            <h3>📖 질환 백과: {{ currentDiseaseData.title }}</h3>
            <div class="guide-content">
              <div class="guide-item">
                <h4>정의</h4>
                <p>{{ currentDiseaseData.definition }}</p>
              </div>
              <div class="guide-item">
                <h4>주요 증상</h4>
                <p>{{ currentDiseaseData.symptoms }}</p>
              </div>
              <div class="guide-item">
                <h4>원인</h4>
                <p>{{ currentDiseaseData.causes }}</p>
              </div>
              <div class="guide-item highlight">
                <h4>치료 및 관리</h4>
                <ul>
                  <li v-for="(line, idx) in currentDiseaseData.care" :key="idx">
                    {{ line }}
                  </li>
                </ul>
              </div>
              <div class="source-info">출처: 서울아산병원</div>
            </div>
          </section>

          <!-- 추천 식단 목록 -->
          <main class="recommendations-list">
            <h3>AI 추천 식단</h3>
            
            <div v-if="diet_loading" class="loading-container">
              <p>AI가 맞춤 식단을 생성하는 중입니다...</p>
            </div>

            <div v-else-if="isRecommendationEmpty" class="create-recommendation-prompt">
              <p>이 분석 결과에 대한 맞춤 식단 추천을 받아보세요.</p>
              <button @click="handleCreateRecommendation" class="create-button">
                ✨ 식단 추천 생성하기
              </button>
              <p v-if="diet_error" class="error-msg">{{ diet_error }}</p>
            </div>

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
                
                <div v-if="rec.embedUrl" class="video-container">
                  <div class="video-wrapper">
                    <iframe :src="rec.embedUrl" title="Recipe Video" frameborder="0" allowfullscreen></iframe>
                  </div>
                </div>
              </div>
            </div>
          </main>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import { storeToRefs } from 'pinia'
import { searchRecipeVideo } from '@/api/youtube'

const props = defineProps({
  photoId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close'])
const router = useRouter()

const goToDermatologist = () => {
  emit('close') // 모달 닫기
  router.push({ name: 'dermatologist' }) // 피부과 찾기 페이지로 이동
}

const analysisStore = useAnalysisStore()
const { user_photo, user_analysis_result, diet_recommendations, diet_loading, diet_error } = storeToRefs(analysisStore)

const loading = ref(true)

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
    const val = res[item.key] !== undefined ? res[item.key] : (res[item.key.replace('prob_', 'prob')] || 0)
    return { ...item, value: Number(val) }
  }).sort((a, b) => b.value - a.value)
})

// 질환별 백과사전 데이터
const diseaseInfoMap = {
  '아토피': {
    title: '아토피성 피부염',
    definition: '심한 가려움증을 동반하고 만성적으로 재발하는 피부 습진 질환으로, 천식, 알레르기 비염과 함께 대표적인 알레르기 질환입니다.',
    symptoms: '심한 가려움증(특히 저녁), 피부 건조, 습진성 변화가 특징입니다. 연령에 따라 발생 부위가 다르며, 긁어서 생기는 습진이 악순환을 유발합니다.',
    causes: '유전적 소인(가족력), 환경적 요인(공해, 인스턴트 식품), 면역학적 이상(IgE 증가), 피부 보호막 이상 등이 복합적으로 작용합니다.',
    care: [
      '원인/유발 인자 제거 및 적절한 목욕과 보습으로 청결 유지',
      '필요 시 국소 스테로이드, 항히스타민제 등 사용',
      '심한 경우 광선 치료나 면역 억제제 전문 치료 고려'
    ]
  },
  '건선': {
    title: '건선 (Psoriasis)',
    definition: '경계가 뚜렷한 홍반 위에 은백색의 비늘(인설)이 겹겹이 쌓이는 만성 염증성 피부 질환입니다. 상피의 과다 증식이 특징입니다.',
    symptoms: '대칭적 발생(무릎, 팔꿈치, 두피 등), 인설 제거 시 점상 출혈 발생, 손발톱 변형(함몰, 박리 등)이 동반될 수 있습니다.',
    causes: '유전적 요인 기반 위에 피부 외상, 감염, 차고 건조한 기후, 스트레스, 약물 등이 유발 인자로 작용합니다.',
    care: [
      '피부 마찰이나 상처 주의 (때 밀기 금지)',
      '겨울철 등 차고 건조한 기후에 보습 철저',
      '연고 도포(국소 치료), 광선 치료, 중증 시 전신 치료제 복용'
    ]
  },
  '여드름': {
    title: '여드름 (Acne)',
    definition: '모낭 피지선에 발생하는 만성 염증성 질환입니다. 피지가 배출되지 못하고 모낭에 갇혀 박테리아가 번식하면서 염증이 생깁니다.',
    symptoms: '면포(블랙헤드, 화이트헤드), 붉은 구진, 곪는 농포가 특징이며, 심하면 결절이나 낭종이 형성됩니다.',
    causes: '호르몬 변화(안드로겐 등), 세균 감염, 스트레스, 수면 부족, 유분이 많은 화장품 사용 등.',
    care: [
      '피지 배출을 돕기 위한 모낭 관리 및 염증/세균 성장 억제',
      '상태에 따라 전문가 처방을 통한 약물 치료(바르는 약, 먹는 약)',
      '흉터 및 색소 침착 방지를 위한 초기 치료와 압출/레이저 등 외과적 치료 고려'
    ]
  },
  '주사': {
    title: '주사 (Rosacea)',
    definition: '얼굴 중앙 부위(코, 뺨 등)의 혈관이 확장되어 지속적인 홍반, 구진, 고름집 등이 생기는 만성 질환입니다. 흔히 딸기코라고도 불립니다.',
    symptoms: '간헐적인 안면 홍조, 지속적인 붉은기(홍반), 실핏줄(모세혈관) 확장, 모공 확대 등이 나타나며 심하면 피부가 두꺼워집니다.',
    causes: '유전적 요인(체질), 만성적인 햇빛 노출, 내분비 이상, 헬리코박터 감염, 음주 및 카페인 과다 섭취 등 복합적입니다.',
    care: [
      '과도한 열, 한랭, 자외선 노출 피하기 (선크림 필수)',
      '음주 및 맵고 뜨거운 자극적인 음식 섭취 자제',
      '증상에 따라 국소 연고, 항생제 복용 또는 혈관 레이저 치료 시행'
    ]
  },
  '지루': {
    title: '지루성 피부염 (Seborrheic Dermatitis)',
    definition: '피지 분비가 많은 부위(머리, 얼굴 등)에 발생하는 만성 염증성 습진 질환입니다. 홍반과 가느다란 인설(비듬)이 주요 특징입니다.',
    symptoms: '두피의 비듬, 얼굴(코, 눈썹 주변)의 붉은 발진과 하얀 각질이 나타나며 가려움증이 동반되기도 합니다.',
    causes: '피지선의 과도한 활동, 효모균 증식, 스트레스, 피로, 낮은 온도와 습도 등 환경적 요인이 복합적으로 작용합니다.',
    care: [
      '약용 샴푸를 사용하여 꾸준한 두피 관리 (샴푸 시 5~10분 방치 후 세정)',
      '세척력이 강한 비누나 알코올 성분 화장품(면도 로션 등) 사용 자제',
      '음주, 사우나, 스트레스, 기름진 음식을 피하는 생활 습관 개선'
    ]
  }
}

const currentDiseaseData = computed(() => {
  const name = user_analysis_result.value?.diagnosisName || user_analysis_result.value?.diagnosis_name || ''
  if (name.includes('아토피')) return diseaseInfoMap['아토피']
  if (name.includes('건선')) return diseaseInfoMap['건선']
  if (name.includes('여드름')) return diseaseInfoMap['여드름']
  if (name.includes('주사')) return diseaseInfoMap['주사']
  if (name.includes('지루')) return diseaseInfoMap['지루']
  return null
})

const isRecommendationEmpty = computed(() => diet_recommendations.value.length === 0)

// 유튜브 정보 로딩
watch(diet_recommendations, async (newVal) => {
  if (newVal && newVal.length > 0) {
    await fetchYoutubeInfoForList();
  }
});

async function fetchYoutubeInfoForList() {
  const promises = diet_recommendations.value.map(async (rec) => {
    if (!rec.embedUrl && rec.menuName) {
      const videoInfo = await searchRecipeVideo(rec.menuName);
      if (videoInfo) rec.embedUrl = videoInfo.embedUrl;
    }
  });
  await Promise.all(promises);
}

onMounted(async () => {
  loading.value = true
  analysisStore.diet_recommendations = [] // 초기화
  
  try {
    await Promise.all([
      analysisStore.fetchPhoto(props.photoId),
      analysisStore.fetchAnalysisResultByPhotoId(props.photoId)
    ])

    const analysisId = user_analysis_result.value?.analysisId
    if (analysisId) {
      const header = await analysisStore.fetchRecommendationHeaderByAnalysisId(analysisId)
      if (header && header.recId) {
        await analysisStore.fetchDietRecommendationById(header.recId)
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function handleCreateRecommendation() {
  const analysisId = user_analysis_result.value?.analysisId
  if (!analysisId) return
  await analysisStore.createAndFetchDietRecommendation({ analysisId, memo: "식단 추천" })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.modal-window {
  background: white;
  width: 90%;
  max-width: 900px;
  height: 85vh;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
  animation: modalUp 0.3s ease-out;
}

@keyframes modalUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

.modal-content-scroll {
  height: 100%;
  overflow-y: auto;
  padding: 40px;
}

/* 기존 AnalysisDetail 스타일 일부 재사용 및 조정 */
.detail-container {
  max-width: 800px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
}

.photo-container {
  flex: 0 0 240px;
}

.user-photo-img {
  width: 240px;
  height: 240px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 12px;
}

.find-hospital-btn {
  width: 100%;
  padding: 12px;
  background: #f0f7ff;
  color: #3182ce;
  border: 1px solid #bee3f8;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.find-hospital-btn:hover {
  background: #ebf8ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(49, 130, 206, 0.15);
}

.info-stack {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.diagnosis-title {
  font-size: 32px;
  font-weight: 800;
  color: #333;
  margin: 0 0 16px 0;
}

.info-field {
  margin-bottom: 24px;
}

.info-label {
  background: #eaddff;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  margin-right: 8px;
  color: #6b55c7;
}

.probabilities-chart {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 12px;
}

.probabilities-chart h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #666;
}

.prob-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 14px;
}

.prob-name {
  width: 80px;
  font-weight: 500;
}

.prob-bar-bg {
  flex: 1;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.prob-bar-fill {
  height: 100%;
  border-radius: 4px;
}

.prob-percent {
  width: 45px;
  text-align: right;
  font-weight: 600;
}

/* Disease Guide */
.disease-guide {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 40px;
}

.disease-guide h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
}

.guide-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.guide-item h4 {
  font-size: 16px;
  color: #6b55c7;
  margin-bottom: 8px;
}

.guide-item p {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin: 0;
}

.guide-item.highlight {
  grid-column: 1 / -1;
  background: #f8f5ff;
  padding: 16px;
  border-radius: 8px;
}

.guide-item ul {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  color: #555;
}

.guide-item li {
  margin-bottom: 4px;
}

.source-info {
  grid-column: 1 / -1;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

@media (max-width: 600px) {
  .guide-content {
    grid-template-columns: 1fr;
  }
}

/* Recommendations */
.recommendations-list h3 {
  font-size: 22px;
  margin-bottom: 24px;
  border-bottom: 2px solid #6b55c7;
  padding-bottom: 12px;
  color: #333;
}

.create-recommendation-prompt {
  text-align: center;
  padding: 60px;
  background: #fcfaff;
  border-radius: 16px;
  border: 2px dashed #d0c4f3;
}

.create-button {
  background: linear-gradient(135deg, #6b55c7, #8e44ad);
  color: white;
  border: none;
  padding: 14px 32px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 50px;
  cursor: pointer;
  margin-top: 16px;
  box-shadow: 0 4px 15px rgba(107, 85, 199, 0.3);
  transition: transform 0.2s;
}

.create-button:hover {
  transform: translateY(-2px);
}

.recommendation-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header h4 {
  margin: 0;
  font-size: 20px;
  color: #6b55c7;
}

.calories {
  font-weight: 600;
  color: #e67e22;
}

.notes {
  background: #f1f8e9;
  padding: 12px;
  border-radius: 8px;
  margin-top: 12px;
  font-size: 14px;
}

.video-container {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.video-wrapper {
  position: relative;
  padding-bottom: 56.25%;
  height: 0;
}

.video-wrapper iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

@media (max-width: 700px) {
  .detail-header {
    flex-direction: column;
  }
  .photo-container {
    flex: none;
    text-align: center;
  }
  .user-photo-img {
    width: 100%;
    max-width: 300px;
  }
}
</style>
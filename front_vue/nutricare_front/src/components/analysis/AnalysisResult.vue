<template>
  <section class="analysis-result">
    <div v-if="loading" class="loading-msg">분석 결과를 불러오는 중입니다...</div>
    
    <div v-else class="result-container">
      <div class="analysis-top-row">
        <div class="result-visual">
          <img v-if="user_photo.photoUrl" :src="user_photo.photoUrl" alt="업로드한 사진" class="uploaded-img" />
          <div v-else class="photo-placeholder">사진을 불러올 수 없습니다.</div>
        </div>

        <div class="analysis-details">
          <div class="diagnosis-box">
            <p class="label">AI 진단 결과</p>
            <!-- diagnosisName이 없으면 diagnosis_name 확인 (DTO 매핑 대응) -->
            <h2 class="diagnosis-name" v-if="user_analysis_result.diagnosisName || user_analysis_result.diagnosis_name">
              "{{ user_analysis_result.diagnosisName || user_analysis_result.diagnosis_name }}"
            </h2>
            <p class="diagnosis-name" v-else>
              진단 결과가 없습니다.
            </p>
            <p class="disclaimer">* 이 결과는 참고용이며, 정확한 진단은 전문의와 상담하세요.</p>
          </div>

          <!-- 확률 분포 차트 -->
          <div class="probabilities-container" v-if="hasProbabilities">
            <h3>상세 분석 결과</h3>
            <ul class="prob-list">
              <li v-for="item in sortedProbabilities" :key="item.key" class="prob-item">
                <div class="prob-header">
                  <span class="prob-label">{{ item.label }}</span>
                  <span class="prob-value">{{ (item.value * 100).toFixed(1) }}%</span>
                </div>
                <div class="progress-bg">
                  <div class="progress-fill" :style="{ width: `${item.value * 100}%`, backgroundColor: item.color }"></div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 질환 정보 가이드 (펼치기/접기 적용) -->
      <section class="disease-guide" v-if="currentDiseaseData">
        <div class="guide-header" @click="toggleGuide" :class="{ 'is-open': showGuide }">
          <h3>📖 질환 백과: {{ currentDiseaseData.title }}</h3>
          <span class="toggle-icon">{{ showGuide ? '▲ 접기' : '▼ 펼쳐서 보기' }}</span>
        </div>
        
        <transition name="fade-slide">
          <div class="guide-content" v-if="showGuide">
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
        </transition>
      </section>

      <div class="actions">
        <button class="primary" type="button" @click="goDiet">
          맞춤 식단 추천 받기
        </button>
        <button class="secondary" type="button" @click="goMyAnalysisList">
          내 분석 목록 보기
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import { useUserStore } from '@/stores/user' // user store import 추가
import { storeToRefs } from 'pinia'

const router = useRouter()
const route = useRoute()
const analysisStore = useAnalysisStore()
const userStore = useUserStore() // user store 인스턴스 생성

// 스토어 상태를 반응형으로 가져옴
const { user_analysis_result, user_photo } = storeToRefs(analysisStore)
const loading = ref(true)
const showGuide = ref(false) // 가이드 표시 여부 상태 추가

function toggleGuide() {
  showGuide.value = !showGuide.value
}

// URL 파라미터에서 ID 가져오기 (router/index.js의 path: 'result/:resultId' 참고)
// 여기서 resultId는 실제로는 photoId 역할을 합니다.
const photoId = route.params.photoId

// 확률 데이터 매핑 정보
const probabilityMap = [
  { key: 'prob_gunsun', label: '건선', color: '#FF6B6B' },
  { key: 'prob_atopy', label: '아토피', color: '#4ECDC4' },
  { key: 'prob_acne', label: '여드름', color: '#FFE66D' },
  { key: 'prob_rosacea', label: '주사', color: '#FF9F43' },
  { key: 'prob_seborr', label: '지루성 피부염', color: '#1A535C' },
  { key: 'prob_normal', label: '정상', color: '#6ab04c' },
]

// 확률 데이터 존재 여부 확인
const hasProbabilities = computed(() => {
  const res = user_analysis_result.value
  return res && (
    res.prob_gunsun !== undefined || 
    res.probGunsun !== undefined || 
    res.prob_normal !== undefined
  )
})

// 확률 데이터를 배열로 변환하고 내림차순 정렬
const sortedProbabilities = computed(() => {
  const res = user_analysis_result.value
  if (!res) return []

  // DTO가 snake_case로 오는지 camelCase로 오는지 불확실하므로 둘 다 체크
  return probabilityMap.map(item => {
    // snake_case 우선 확인 후 camelCase 확인 (probGunsun)
    const val = res[item.key] !== undefined ? res[item.key] : (res[item.key.replace('prob_', 'prob')] || 0)
    return {
      ...item,
      value: Number(val)
    }
  }).sort((a, b) => b.value - a.value) // 높은 순 정렬
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

onMounted(async () => {
  if (photoId) {
    try {
      // 1. 사진 정보(URL) 가져오기
      await analysisStore.fetchPhoto(photoId)
      // 2. 진단 결과 가져오기
      await analysisStore.fetchAnalysisResultByPhotoId(photoId)
    } catch (e) {
      console.error(e)
      alert("결과를 불러오는데 실패했습니다.")
    } finally {
      loading.value = false
    }
  } else {
    alert("잘못된 접근입니다.")
    router.replace('/analysis')
  }
})

function goDiet() {
  // 식단 추천 생성/상세 페이지로 이동 (photoId를 기반으로 생성 요청을 하거나 조회)
  // 예시: analysisDetail로 가면서 photoId를 넘김
  router.push({ name: 'analysisDetail', params: { photoId: photoId } }) // resultId 대신 photoId 사용
}

function goMyAnalysisList() {
  if (userStore.userId) {
    router.push({ name: 'analysisList', params: { userId: userStore.userId } }).catch(() => {})
  } else {
    alert("사용자 정보를 불러올 수 없습니다.")
    // 로그인 페이지 등으로 리디렉션하거나 다른 처리
  }
}
</script>

<style scoped>
.analysis-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 32px 16px 48px;
  background: #f8f5eb;
  min-height: 400px;
}

.result-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  width: 100%;
  max-width: 1000px;
}

.analysis-top-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: center;
  gap: 40px;
  width: 100%;
}

.result-visual {
  flex: 0 0 400px; /* 고정 너비 느낌 */
  aspect-ratio: 1;
  background: #e0e0e0;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.analysis-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 300px;
}

.uploaded-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.diagnosis-box {
  text-align: left;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.label {
  font-size: 14px;
  color: #888;
  margin-bottom: 8px;
  font-weight: 600;
}

.diagnosis-name {
  font-size: 28px;
  font-weight: 800;
  color: #6b55c7;
  margin: 0;
}

.disclaimer {
  margin-top: 12px;
  font-size: 13px;
  color: #e67e22;
  font-weight: 600;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 12px;
}

.primary {
  padding: 14px 28px;
  background: #6b55c7;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.primary:hover {
  background: #5a45b0;
  transform: translateY(-2px);
}

.secondary {
  padding: 14px 28px;
  background: #fff;
  color: #555;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary:hover {
  background: #f9f9f9;
  border-color: #bbb;
}

.loading-msg {
  color: #888;
  margin-top: 40px;
}

.probabilities-container {
  width: 100%;
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.probabilities-container h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 700;
}

.prob-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.prob-item {
  width: 100%;
}

.prob-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 15px;
  color: #555;
}

.prob-value {
  font-weight: 700;
  color: #333;
}

.progress-bg {
  width: 100%;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* Disease Guide Style */
.disease-guide {
  width: 100%;
  background: #fff;
  border: 1px solid #efefef;
  border-radius: 16px;
  margin-top: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.guide-header {
  padding: 20px 32px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s;
}

.guide-header:hover {
  background: #fcfaff;
}

.guide-header h3 {
  font-size: 20px;
  color: #333;
  margin: 0;
  font-weight: 700;
}

.toggle-icon {
  font-size: 14px;
  color: #6b55c7;
  font-weight: 600;
}

.guide-content {
  padding: 0 32px 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  border-top: 1px solid #f8f5ff;
  padding-top: 24px;
}

.guide-item h4 {
  font-size: 17px;
  color: #6b55c7;
  margin-bottom: 10px;
  font-weight: 700;
}

.guide-item p {
  font-size: 15px;
  color: #555;
  line-height: 1.7;
  margin: 0;
}

.guide-item.highlight {
  grid-column: 1 / -1;
  background: #f8f5ff;
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #6b55c7;
}

.guide-item ul {
  margin: 0;
  padding-left: 20px;
  font-size: 15px;
  color: #555;
}

.guide-item li {
  margin-bottom: 6px;
}

.source-info {
  grid-column: 1 / -1;
  text-align: right;
  font-size: 13px;
  color: #aaa;
  margin-top: 12px;
}

/* Transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 900px) {
  .analysis-top-row {
    flex-direction: column;
    align-items: center;
  }

  .result-visual {
    flex: 0 0 auto;
    width: 100%;
    max-width: 450px;
  }
}

@media (max-width: 768px) {
  .guide-content {
    grid-template-columns: 1fr;
  }
}
</style>
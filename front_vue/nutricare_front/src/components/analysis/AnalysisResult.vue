<template>
  <section class="analysis-result">
    <div v-if="loading" class="loading-msg">분석 결과를 불러오는 중입니다...</div>
    
    <div v-else class="result-container">
      <div class="result-visual">
        <img v-if="user_photo.photoUrl" :src="user_photo.photoUrl" alt="업로드한 사진" class="uploaded-img" />
        <div v-else class="photo-placeholder">사진을 불러올 수 없습니다.</div>
      </div>

      <div class="diagnosis-box">
        <p class="label">AI 진단 결과</p>
        <h2 class="diagnosis-name" v-if="user_analysis_result.diagnosisName">
          "{{ user_analysis_result.diagnosisName }}"
        </h2>
        <p class="diagnosis-name" v-else>
          진단 결과가 없습니다.
        </p>
      </div>

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

// URL 파라미터에서 ID 가져오기 (router/index.js의 path: 'result/:resultId' 참고)
// 여기서 resultId는 실제로는 photoId 역할을 합니다.
const photoId = route.params.photoId

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
  gap: 20px;
  width: 100%;
}

.result-visual {
  width: min(420px, 90vw);
  aspect-ratio: 1; /* 정사각형 */
  background: #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.uploaded-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.diagnosis-box {
  text-align: center;
}

.label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.diagnosis-name {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.actions {
  margin-top: 10px;
  display: flex; /* 버튼들을 가로로 나열하기 위해 flex 추가 */
  gap: 10px; /* 버튼들 사이의 간격 */
}

.primary {
  padding: 12px 24px;
  background: #6b55c7; /* 브랜드 컬러 예시 */
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.primary:hover {
  background: #5a45b0;
}

/* Secondary button style */
.secondary {
  padding: 12px 24px;
  background: #f0f0f0; /* 밝은 배경 */
  color: #333; /* 어두운 텍스트 */
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.secondary:hover {
  background: #e0e0e0;
}

.loading-msg {
  color: #888;
  margin-top: 40px;
}
</style>
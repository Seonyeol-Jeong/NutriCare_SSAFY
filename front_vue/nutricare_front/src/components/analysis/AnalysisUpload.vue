<template>
  <section class="analysis-upload">
    <div class="dropzone" @click="onPickFile">
      <template v-if="previewUrl">
        <img :src="previewUrl" alt="업로드 미리보기" class="preview-img" />
        <p class="filename" v-if="selectedFile">{{ selectedFile.name }}</p>
        <p class="hint sub">다시 선택하려면 클릭하세요</p>
      </template>
      <template v-else>
        <div class="icon">📷</div>
        <p class="hint">이미지를 업로드하세요</p>
      </template>
      <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="onFileChange" />
    </div>

    <div class="health-form">
      <div class="field">
        <label for="height">키 (cm)</label>
        <input id="height" v-model.number="healthProfile.heightCm" type="number" step="0.1" placeholder="예) 172.5" />
      </div>
      <div class="field">
        <label for="weight">몸무게 (kg)</label>
        <input id="weight" v-model.number="healthProfile.weightKg" type="number" step="0.1" placeholder="예) 63.2" />
      </div>

      <div class="field radios" role="group" aria-label="활동도">
        <span class="label">활동도</span>
        <div class="options">
          <label><input v-model="healthProfile.activityLevel" type="radio" value="LOW" /> 하(운동 없음)</label>
          <label><input v-model="healthProfile.activityLevel" type="radio" value="MEDIUM" /> 중(주 2-3회)</label>
          <label><input v-model="healthProfile.activityLevel" type="radio" value="HIGH" /> 상(주 4회 이상)</label>
        </div>
      </div>

      <div class="field radios" role="group" aria-label="목표">
        <span class="label">목표</span>
        <div class="options">
          <label><input v-model="healthProfile.goalType" type="radio" value="LOSS" /> 감량</label>
          <label><input v-model="healthProfile.goalType" type="radio" value="MAINTAIN" /> 유지</label>
          <label><input v-model="healthProfile.goalType" type="radio" value="GAIN" /> 증량</label>
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="primary" type="button" @click="analyze">분석하기</button>
      <span class="arrow">→</span>
      <span class="spinner" aria-live="polite">준비중</span>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const analysisStore = useAnalysisStore()
const userStore = useUserStore()
const { healthProfile } = storeToRefs(userStore)
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')

// 기본값 보정
if (!healthProfile.value || typeof healthProfile.value !== 'object') {
  healthProfile.value = {}
}
healthProfile.value.heightCm ??= null
healthProfile.value.weightKg ??= null
healthProfile.value.activityLevel ??= 'LOW'
healthProfile.value.goalType ??= 'MAINTAIN'

function onPickFile() {
  fileInput.value?.click()
}

function onFileChange(event) {
  const files = event.target.files || []
  if (!files.length) {
    // 사용자가 취소를 누른 경우 이전 선택 유지
    return
  }
  const [file] = files
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

async function analyze() {
  if (!selectedFile.value) {
    alert('이미지를 먼저 선택해주세요.')
    return
  }

  try {
    const photoResp = await analysisStore.uploadPhoto(selectedFile.value)
    // TODO: 건강 정보 전송 API가 별도로 있다면 여기서 호출
    const resultId = photoResp?.photoId || 'result'
    console.log('uploaded photo', photoResp, 'healthProfile', healthProfile.value)
    router.push({ name: 'analysisResult', params: { resultId } }).catch(() => {})
  } catch (err) {
    console.error(err)
    alert('업로드에 실패했습니다.')
  }
}
</script>

<style scoped>
.analysis-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 32px 16px 48px;
  background: #f8f5eb;
}

.dropzone {
  width: min(420px, 90vw);
  aspect-ratio: 4 / 3;
  background: #e9e2f3;
  border-radius: 12px;
  display: grid;
  place-items: center;
  gap: 10px;
  color: #5b4b82;
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.icon {
  font-size: 32px;
}

.hint {
  margin: 0;
  color: #6a6680;
}

.hint.sub {
  font-size: 13px;
  color: #7a7690;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.filename {
  position: absolute;
  left: 50%;
  bottom: 10px;
  transform: translateX(-50%);
  font-size: 14px;
  color: #444;
  background: rgba(255, 255, 255, 0.8);
  padding: 6px 10px;
  border-radius: 10px;
  max-width: calc(100% - 20px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hidden-input {
  display: none;
}

.health-form {
  width: min(420px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #333;
}

.field input {
  border: 1px solid #c7c7c7;
  border-radius: 6px;
  padding: 8px;
  font-size: 14px;
}

.radios .options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-weight: 600;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.primary {
  padding: 8px 14px;
  background: #d8d8d8;
  border: 1px solid #aeaeae;
  cursor: pointer;
}

.arrow {
  color: #555;
}

.spinner {
  color: #555;
}
</style>

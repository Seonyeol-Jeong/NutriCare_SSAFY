<template>
  <div class="upload-container ">
    <h2 class="title">AI 피부 분석</h2>
    <p class="subtitle">얼굴 사진을 업로드하여 피부 상태를 분석하고 맞춤 식단을 추천받아 보세요.</p>
    
    <!-- 파일 업로드 영역 -->
    <div 
      class="drop-zone"
      :class="{ 'is-dragover': isDragover }"
      @click="onPickFile"
      @dragover.prevent="isDragover = true"
      @dragleave.prevent="isDragover = false"
      @drop.prevent="handleDrop"
    >
      <!-- 파일이 없을 때 (초기 상태) -->
      <div v-if="!previewUrl" class="initial-state">
        <svg class="upload-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
        <p class="main-hint">사진을 드래그하거나 여기를 클릭하세요</p>
        <p class="sub-hint">JPG, PNG, WEBP 등 이미지 파일</p>
      </div>

      <!-- 파일이 있을 때 (미리보기 상태) -->
      <div v-else class="preview-area">
        <img :src="previewUrl" alt="업로드 미리보기" class="preview-image" />
        <div class="file-info">
          <p>{{ selectedFile.name }}</p>
          <small>{{ (selectedFile.size / 1024).toFixed(1) }} KB</small>
        </div>
        <button @click.stop="removeFile" class="remove-button" aria-label="파일 삭제">×</button>
      </div>

      <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="onFileChange" />
    </div>

    <!-- 건강 정보 폼 -->
    <div class="health-form">
       <!-- 폼 내용 생략 (기존과 동일) -->
    </div>

    <!-- 분석하기 버튼 -->
    <div class="actions">
      <button class="primary-button" type="button" @click="analyze" :disabled="!selectedFile || isLoading">
        <span v-if="isLoading" class="spinner"></span>
        {{ isLoading ? '분석 중...' : '분석하기' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
// useUserStore와 storeToRefs는 health-form을 위해 필요하다면 유지
// import { storeToRefs } from 'pinia'
// import { useUserStore } from '@/stores/user'

const router = useRouter()
const analysisStore = useAnalysisStore()

const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const isDragover = ref(false)
const isLoading = ref(false)

function onPickFile() {
  fileInput.value?.click()
}

function handleFile(file) {
  if (!file || !file.type.startsWith('image/')) {
    alert('이미지 파일만 업로드할 수 있습니다.')
    return
  }
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

function onFileChange(event) {
  const [file] = event.target.files || []
  handleFile(file)
}

function handleDrop(event) {
  isDragover.value = false
  const [file] = event.dataTransfer.files || []
  handleFile(file)
}

function removeFile() {
  selectedFile.value = null
  previewUrl.value = ''
  // 파일 인풋의 값도 초기화하여 같은 파일을 다시 선택할 수 있게 함
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

async function analyze() {
  if (!selectedFile.value) {
    alert('이미지를 먼저 선택해주세요.')
    return
  }
  isLoading.value = true
  try {
    const photoResp = await analysisStore.uploadPhoto(selectedFile.value)
    const photoId = photoResp?.photoId
    if (photoId) {
      router.push({ name: 'analysisResult', params: { photoId } })
    } else {
      alert('분석 결과 ID를 받지 못했습니다.')
    }
  } catch (err) {
    console.error('업로드 실패:', err)
    alert('업로드에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.upload-container {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 32px;
  background-color: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  font-size: 28px;
  color: #333;
  margin: 0;
}

.subtitle {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin: -16px 0 0;
}

.drop-zone {
  width: 100%;
  aspect-ratio: 16 / 9;
  border: 2px dashed #d1c4e9;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
  position: relative;
  overflow: hidden;
}

.drop-zone:hover, .drop-zone.is-dragover {
  background-color: #f8f5eb;
  border-color: #6b55c7;
}

.initial-state {
  text-align: center;
  color: #6b55c7;
}

.upload-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 12px;
}

.main-hint {
  font-weight: 600;
  font-size: 16px;
  margin: 0;
}

.sub-hint {
  font-size: 13px;
  color: #999;
  margin: 4px 0 0;
}

.preview-area {
  width: 100%;
  height: 100%;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-info {
  position: absolute;
  bottom: 12px;
  left: 12px;
  right: 12px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-info small {
  display: block;
  opacity: 0.8;
}

.remove-button {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border: none;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  font-size: 20px;
  line-height: 28px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.remove-button:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.hidden-input {
  display: none;
}

.actions {
  display: flex;
}

.primary-button {
  flex-grow: 1;
  padding: 14px;
  background: #6b55c7;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.primary-button:hover:not(:disabled) {
  background-color: #5a45b0;
}

.primary-button:disabled {
  background-color: #c5bada;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 건강 정보 폼 스타일은 생략. 필요 시 이전 스타일을 참고하여 추가 */
.health-form { display: none; } /* 임시로 숨김 */
</style>
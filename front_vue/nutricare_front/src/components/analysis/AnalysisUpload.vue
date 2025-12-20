<template>
  <div class="upload-page-container">
    <div class="upload-container">
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

      <!-- 분석하기 버튼 -->
      <div class="actions">
        <button class="primary-button" type="button" @click="analyze" :disabled="!selectedFile || isLoading">
          <span v-if="isLoading" class="spinner"></span>
          {{ isLoading ? '분석 중...' : '분석하기' }}
        </button>
      </div>
    </div>

    <!-- 회원정보 입력 안내 및 폼 (분석하기 버튼 아래에 노출) -->
    <div v-if="showProfileForm" class="profile-form-section" ref="profileFormRef">
      <div class="info-alert">
        <i class="bi bi-info-circle-fill"></i>
        <span>회원정보를 입력해서 더 정확한 분석을 받으세요.</span>
      </div>
      <UserProfileForm />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalysisStore } from '@/stores/analysis'
import { useUserStore } from '@/stores/user'
import UserProfileForm from '@/components/user/UserProfileForm.vue'

const router = useRouter()
const analysisStore = useAnalysisStore()
const userStore = useUserStore()

const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const isDragover = ref(false)
const isLoading = ref(false)
const showProfileForm = ref(false)
const profileFormRef = ref(null)

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
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

async function analyze() {
  if (!selectedFile.value) {
    alert('이미지를 먼저 선택해주세요.')
    return
  }

  // 회원 정보 확인
  await userStore.fetchMe()
  const hasHealthProfile = userStore.healthProfile && Object.keys(userStore.healthProfile).length > 0
  
  if (!hasHealthProfile) {
    showProfileForm.value = true
    // 폼이 나타난 후 해당 위치로 스크롤
    await nextTick()
    profileFormRef.value?.scrollIntoView({ behavior: 'smooth' })
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
.upload-page-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
  align-items: center;
  width: 100%;
  padding-bottom: 50px;
}

.upload-container {
  width: 100%;
  max-width: 500px;
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

/* Profile Form Section */
.profile-form-section {
  width: 100%;
  max-width: 800px;
  animation: fadeIn 0.5s ease-out;
}

.info-alert {
  background-color: #e8f0fe;
  border-left: 5px solid #4285f4;
  padding: 16px;
  margin-bottom: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1967d2;
  font-weight: 500;
}

.info-alert i {
  font-size: 20px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
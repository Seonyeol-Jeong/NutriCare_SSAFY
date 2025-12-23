<template>
  <div class="profile-page-wrapper">
    <div class="unified-profile-card">
      <!-- 왼쪽 사이드 (프로필 요약) -->
      <aside class="profile-side">
        <div class="avatar-circle">
          {{ userInitial }}
        </div>
        <h1 class="user-name">{{ form.name || '사용자' }}</h1>
        <p class="user-email">{{ form.email || '이메일 정보 없음' }}</p>
        
        <div class="side-actions">
          <button class="action-btn primary" @click="onEditProfile">
            정보 수정
          </button>
          <button class="action-btn secondary" @click="onEditPassword">
            비밀번호 변경
          </button>
        </div>
      </aside>

      <!-- 오른쪽 메인 (상세 정보) -->
      <main class="info-side">
        <section class="info-group">
          <h3 class="group-title">기본 정보</h3>
          <div class="info-row">
            <div class="info-item">
              <span class="label">출생연도</span>
              <span class="val">{{ form.birthYear ? form.birthYear + '년' : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">성별</span>
              <span class="val">{{ formatGender(form.gender) }}</span>
            </div>
          </div>
        </section>

        <div class="divider"></div>

        <section class="info-group">
          <h3 class="group-title">건강 프로필</h3>
          <div class="info-grid-compact">
            <div class="info-item">
              <span class="label">키</span>
              <span class="val">{{ form.height ? form.height + 'cm' : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">체중</span>
              <span class="val">{{ form.weight ? form.weight + 'kg' : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">활동량</span>
              <span class="val">{{ translateActivity(form.activity) }}</span>
            </div>
            <div class="info-item">
              <span class="label">목표</span>
              <span class="val">{{ translateGoal(form.goal) }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  userId: route.params.userid || '',
  email: '',
  name: '',
  birthYear: '',
  gender: '',
  height: '',
  weight: '',
  activity: '',
  goal: '',
})

const userInitial = computed(() => {
  return form.name ? form.name.charAt(0).toUpperCase() : 'U'
})

function syncFromStore() {
  const info = userStore.userInfo || {}
  const profile = userStore.healthProfile || {}

  form.email = info.email || ''
  form.name = info.name || ''
  form.birthYear = info.birthYear ?? ''
  form.gender = info.gender ?? ''
  form.height = profile.heightCm ?? profile.height ?? ''
  form.weight = profile.weightKg ?? profile.weight ?? ''
  form.activity = profile.activityLevel ?? profile.activity ?? ''
  form.goal = profile.goalType ?? profile.goal ?? ''
}

onMounted(() => {
  syncFromStore()
})

watch(
  () => [userStore.userInfo, userStore.healthProfile],
  () => syncFromStore(),
  { deep: true }
)

function formatGender(g) {
  if (g === 'M' || g === 'MALE') return '남성'
  if (g === 'F' || g === 'FEMALE') return '여성'
  return '-'
}

function translateActivity(val) {
  const map = {
    'LOW': '낮음',
    'MEDIUM': '보통',
    'HIGH': '높음',
    'SEDENTARY': '낮음', // 이전 값 대응용
    'MODERATELY_ACTIVE': '보통'
  }
  return map[val] || val || '-'
}

function translateGoal(val) {
  const map = {
    'GAIN': '증량',
    'MAINTAIN': '유지',
    'LOSS': '감량',
    'LOSE_WEIGHT': '감량', // 이전 값 대응용
    'GAIN_MUSCLE': '증량'
  }
  return map[val] || val || '-'
}

function onEditProfile() {
  router.push({ name: 'updateProfile', params: { userid: form.userId || 'me' } }).catch(() => {})
}

function onEditPassword() {
  router.push({ name: 'updatePassword', params: { userid: form.userId || 'me' } }).catch(() => {})
}
</script>

<style scoped>
.profile-page-wrapper {
  padding: 24px 0;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: transparent;
  min-height: auto;
}

.unified-profile-card {
  display: flex;
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.08);
  overflow: hidden; /* 둥근 모서리 유지 */
  border: 1px solid #f0f0f0;
}

/* --- Left Side (Profile) --- */
.profile-side {
  width: 260px;
  background-color: #f8f9fc;
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border-right: 1px solid #edf2f7;
  flex-shrink: 0;
}

.avatar-circle {
  width: 90px;
  height: 90px;
  background: linear-gradient(135deg, #6b55c7, #9b72e0);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  box-shadow: 0 6px 16px rgba(107, 85, 199, 0.2);
}

.user-name {
  font-size: 20px;
  font-weight: 800;
  color: #2d3748;
  margin: 0 0 4px 0;
}

.user-email {
  font-size: 13px;
  color: #718096;
  margin: 0 0 32px 0;
  word-break: break-all;
}

.side-actions {
  margin-top: auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.action-btn.primary {
  background-color: #6b55c7;
  color: white;
}

.action-btn.primary:hover {
  background-color: #5a45b0;
  transform: translateY(-1px);
}

.action-btn.secondary {
  background-color: white;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.action-btn.secondary:hover {
  background-color: #edf2f7;
}

/* --- Right Side (Info) --- */
.info-side {
  flex: 1;
  padding: 40px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.group-title {
  font-size: 14px;
  font-weight: 700;
  color: #6b55c7;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-row {
  display: flex;
  gap: 40px;
}

.info-grid-compact {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px 32px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 12px;
  font-weight: 600;
  color: #a0aec0;
  margin-bottom: 6px;
}

.val {
  font-size: 17px;
  font-weight: 700;
  color: #2d3748;
}

.divider {
  height: 1px;
  background-color: #f1f5f9;
  margin: 32px 0;
  width: 100%;
}

/* Responsive */
@media (max-width: 768px) {
  .unified-profile-card {
    flex-direction: column;
    max-width: 100%;
  }
  
  .profile-side {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #edf2f7;
    padding: 32px 20px;
  }
  
  .info-side {
    padding: 32px 20px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 20px;
  }
  
  .info-grid-compact {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>

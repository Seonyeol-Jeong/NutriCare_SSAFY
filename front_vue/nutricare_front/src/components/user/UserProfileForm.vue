<template>
  <div class="profile-page-wrapper">
    <div class="unified-profile-card">
      <!-- Left Sidebar (Static Visual Anchor) -->
      <aside class="profile-side">
        <div class="avatar-circle">
          {{ userInitial }}
        </div>
        <h1 class="user-name">{{ form.name || '사용자' }}</h1>
        <p class="user-email">{{ userStore.userInfo?.email || '이메일 정보 없음' }}</p>
        
        <div class="side-actions">
          <button class="action-btn secondary" @click="onLater">
            취소 / 돌아가기
          </button>
        </div>
      </aside>

      <!-- Right Main (Edit Form) -->
      <main class="info-side">
        <form @submit.prevent="onSubmit">
          <section class="info-group">
            <h3 class="group-title">기본 정보 수정</h3>
            <div class="form-grid">
              <div class="form-item">
                <label for="name" class="label">이름</label>
                <input id="name" v-model="form.name" type="text" class="input-field" placeholder="이름 입력" required />
              </div>
              <div class="form-item">
                <label for="birthYear" class="label">출생연도</label>
                <input id="birthYear" v-model="form.birthYear" type="number" class="input-field" placeholder="YYYY" />
              </div>
              <div class="form-item full-width">
                <label class="label">성별</label>
                <div class="radio-group">
                  <label v-for="opt in genderOptions" :key="opt.value" class="radio-chip">
                    <input type="radio" v-model="form.gender" :value="opt.value" name="gender" />
                    <span class="chip-text">{{ opt.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </section>

          <div class="divider"></div>

          <section class="info-group">
            <h3 class="group-title">건강 프로필 설정</h3>
            <div class="form-grid">
              <div class="form-item">
                <label for="height" class="label">키 (cm)</label>
                <input id="height" v-model="form.heightCm" type="number" step="0.1" class="input-field" placeholder="000.0" />
              </div>
              <div class="form-item">
                <label for="weight" class="label">체중 (kg)</label>
                <input id="weight" v-model="form.weightKg" type="number" step="0.1" class="input-field" placeholder="00.0" />
              </div>
              
              <div class="form-item full-width">
                <label class="label">활동량</label>
                <div class="radio-group wrap">
                  <label v-for="opt in activityOptions" :key="opt.value" class="radio-chip">
                    <input type="radio" v-model="form.activityLevel" :value="opt.value" name="activity" />
                    <span class="chip-text">{{ opt.label }}</span>
                  </label>
                </div>
              </div>

              <div class="form-item full-width">
                <label class="label">목표</label>
                <div class="radio-group">
                  <label v-for="opt in goalOptions" :key="opt.value" class="radio-chip">
                    <input type="radio" v-model="form.goalType" :value="opt.value" name="goal" />
                    <span class="chip-text">{{ opt.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </section>

          <div class="form-actions">
            <button type="submit" class="save-btn">저장하기</button>
          </div>
        </form>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  name: '',
  birthYear: '',
  gender: '',
  heightCm: '',
  weightKg: '',
  activityLevel: '',
  goalType: '',
})

const userInitial = computed(() => {
  const name = form.name || userStore.userInfo?.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const genderOptions = [
  { value: 'MALE', label: '남성' },
  { value: 'FEMALE', label: '여성' },
]

const activityOptions = [
  { value: 'LOW', label: '낮음' },
  { value: 'MEDIUM', label: '보통' },
  { value: 'HIGH', label: '높음' },
]

const goalOptions = [
  { value: 'GAIN', label: '증량' },
  { value: 'MAINTAIN', label: '유지' },
  { value: 'LOSS', label: '감량' },
]

function syncFormFromStore() {
  const info = userStore.userInfo || {}
  const profile = userStore.healthProfile || {}

  form.name = info.name ?? ''
  form.birthYear = info.birthYear ?? ''
  form.gender = info.gender ?? 'MALE'
  form.heightCm = profile.heightCm ?? ''
  form.weightKg = profile.weightKg ?? ''
  form.activityLevel = profile.activityLevel ?? 'LOW'
  form.goalType = profile.goalType ?? 'MAINTAIN'
}

onMounted(() => {
  syncFormFromStore()
})

watch(
  () => [userStore.userInfo, userStore.healthProfile],
  () => syncFormFromStore(),
  { deep: true }
)

function normalizeOneDecimal(value) {
  if (value === null || value === undefined || value === '') return ''
  const num = Number(value)
  if (Number.isNaN(num)) return ''
  return Number(num.toFixed(1))
}

async function onSubmit() {
  const payload = {
    user: {
      name: form.name,
      birthYear: form.birthYear,
      gender: form.gender,
    },
    healthProfile: {
      heightCm: normalizeOneDecimal(form.heightCm),
      weightKg: normalizeOneDecimal(form.weightKg),
      activityLevel: form.activityLevel,
      goalType: form.goalType,
    },
  }
  await userStore.updateProfile(payload)
  router.push({ name: 'userDetail' }).catch(() => {})
}

function onLater() {
  router.push({ name: 'userDetail' }).catch(() => {})
}
</script>

<style scoped>
/* Reuse layout styles from UserDetailForm.vue for consistency */
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
  /* overflow: hidden; removed to prevent shadow clipping if needed, but added back if borders need it. 
     Actually, overflow: hidden clips the box-shadow if it's on the card itself. 
     But here the shadow is on .unified-profile-card, so it's outside. */
  overflow: hidden; 
  border: 1px solid #f0f0f0;
  margin: 0 auto;
}

/* Sidebar Profile */
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

.action-btn.secondary {
  background-color: white;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}
.action-btn.secondary:hover {
  background-color: #edf2f7;
}

/* Main Form Section */
.info-side {
  flex: 1;
  padding: 40px 32px;
  display: flex;
  flex-direction: column;
}

.group-title {
  font-size: 14px;
  font-weight: 700;
  color: #6b55c7;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.full-width {
  grid-column: span 2;
}

.label {
  font-size: 12px;
  font-weight: 600;
  color: #a0aec0;
}

.input-field {
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  color: #2d3748;
  outline: none;
  transition: border-color 0.2s;
}

.input-field:focus {
  border-color: #6b55c7;
}

/* Radio Chips */
.radio-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap; /* Ensure all radio groups wrap */
}

.radio-chip {
  cursor: pointer;
}

.radio-chip input {
  display: none;
}

.chip-text {
  display: inline-block;
  padding: 8px 16px;
  background-color: #f7fafc;
  border: 1px solid #edf2f7;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #718096;
  transition: all 0.2s;
}

.radio-chip input:checked + .chip-text {
  background-color: #6b55c7;
  color: white;
  border-color: #6b55c7;
  box-shadow: 0 4px 10px rgba(107, 85, 199, 0.2);
}

.divider {
  height: 1px;
  background-color: #f1f5f9;
  margin: 32px 0;
  width: 100%;
}

.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  background-color: #6b55c7;
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(107, 85, 199, 0.25);
}

.save-btn:hover {
  background-color: #5a45b0;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(107, 85, 199, 0.35);
}

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
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .full-width {
    grid-column: span 1;
  }
}
</style>

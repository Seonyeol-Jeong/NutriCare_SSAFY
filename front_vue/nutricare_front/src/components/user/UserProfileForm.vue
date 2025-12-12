<template>
  <div class="container my-5">
    <div class="card shadow-sm border-light">
      <div class="card-header bg-white py-3">
        <h2 class="mb-0 h5">프로필 수정</h2>
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="onSubmit">
          <div class="row g-3">
            <!-- Basic Fields -->
            <div class="col-12" v-for="field in basicFields" :key="field.key">
              <div class="input-group">
                <span class="input-group-text label-box" :title="field.label">
                  <i :class="field.icon"></i>
                </span>
                <input
                  :id="field.key"
                  v-model="form[field.key]"
                  :type="field.type"
                  class="form-control"
                  :placeholder="field.label"
                  :required="field.required"
                  :step="field.step"
                />
              </div>
            </div>

            <!-- Gender -->
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="성별">
                  <i class="bi bi-gender-ambiguous"></i>
                </span>
                <div class="form-control d-flex align-items-center flex-wrap gap-2 button-group-radio">
                  <template v-for="option in genderOptions" :key="option.value">
                    <input
                      type="radio"
                      class="btn-check"
                      :id="`gender-${option.value}`"
                      v-model="form.gender"
                      :value="option.value"
                      autocomplete="off"
                    />
                    <label class="btn btn-outline-primary btn-sm" :for="`gender-${option.value}`">{{ option.label }}</label>
                  </template>
                </div>
              </div>
            </div>

            <!-- Activity Level -->
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="활동 정도">
                  <i class="bi bi-activity"></i>
                </span>
                <div class="form-control d-flex align-items-center flex-wrap gap-2 button-group-radio">
                  <template v-for="option in activityOptions" :key="option.value">
                    <input
                      type="radio"
                      class="btn-check"
                      :id="`activity-${option.value}`"
                      v-model="form.activityLevel"
                      :value="option.value"
                      autocomplete="off"
                    />
                    <label class="btn btn-outline-primary btn-sm" :for="`activity-${option.value}`">{{ option.label }}</label>
                  </template>
                </div>
              </div>
            </div>

            <!-- Goal Type -->
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="목표">
                  <i class="bi bi-bullseye"></i>
                </span>
                <div class="form-control d-flex align-items-center flex-wrap gap-2 button-group-radio">
                  <template v-for="option in goalOptions" :key="option.value">
                    <input
                      type="radio"
                      class="btn-check"
                      :id="`goal-${option.value}`"
                      v-model="form.goalType"
                      :value="option.value"
                      autocomplete="off"
                    />
                    <label class="btn btn-outline-primary btn-sm" :for="`goal-${option.value}`">{{ option.label }}</label>
                  </template>
                </div>
              </div>
            </div>
          </div>
          <hr class="my-4" />
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="onLater">
              <i class="bi bi-x-lg"></i> 나중에 하기
            </button>
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-check-lg"></i> 수정
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 백엔드 필드명에 맞춘 폼 구조
const form = reactive({
  name: '',
  birthYear: '',
  gender: '',
  heightCm: '',
  weightKg: '',
  activityLevel: '',
  goalType: '',
})

function syncFormFromStore() {
  const info = userStore.userInfo || {}
  const profile = userStore.healthProfile || {}

  form.name = info.name ?? ''
  form.birthYear = info.birthYear  ?? ''
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

const basicFields = [
  { key: 'name', label: '이름', icon: 'bi bi-person', type: 'text', required: true },
  { key: 'birthYear', label: '출생연도', icon: 'bi bi-calendar-event', type: 'number', required: false, step: '1' },
  { key: 'heightCm', label: '키(cm)', icon: 'bi bi-rulers', type: 'number', required: false, step: '0.1' },
  { key: 'weightKg', label: '몸무게(kg)', icon: 'bi bi-speedometer2', type: 'number', required: false, step: '0.1' },
]

const genderOptions = [
  { value: 'MALE', label: '남성' },
  { value: 'FEMALE', label: '여성' },
]

const activityOptions = [
  { value: 'LOW', label: '낮음 (운동 안함)' },
  { value: 'MEDIUM', label: '보통 (주 2-3회)' },
  { value: 'HIGH', label: '높음 (주 4회 이상)' },
]

const goalOptions = [
  { value: 'GAIN', label: '증량' },
  { value: 'MAINTAIN', label: '유지' },
  { value: 'LOSS', label: '감량' },
]

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
.container {
  max-width: 800px;
}
.card {
  border-radius: 0.75rem;
}
.label-box {
  width: 42px;
  justify-content: center;
  background-color: #f8f9fa;
  font-size: 1.1rem;
}
.input-group {
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  overflow: hidden;
}
.input-group .form-control {
  border: none;
}
.input-group .label-box {
  border: none;
}
.btn {
  font-weight: 500;
}
.btn i {
  margin-right: 0.35rem;
  font-size: 0.9em;
}
.form-check-label {
  cursor: pointer;
}
/* Adjustments for radio button groups */
.input-group .form-control {
  height: auto; /* Allow content to dictate height */
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}
.input-group .form-control.d-flex {
  align-items: center;
}
.button-group-radio {
  padding: 0.5rem 0.75rem; /* Adjust padding to make buttons look good */
  border-left: 0;
}
.button-group-radio .btn-check + .btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  border-radius: 0.25rem;
}
.button-group-radio .btn-check:checked + .btn-outline-primary {
  background-color: var(--bs-primary);
  color: var(--bs-white);
}
.button-group-radio .btn-check:checked + .btn-outline-primary:hover {
  background-color: var(--bs-primary);
  color: var(--bs-white);
}
</style>

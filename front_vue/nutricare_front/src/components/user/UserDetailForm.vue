<template>
  <div class="container my-5">
    <div class="card shadow-sm border-light">
      <div class="card-header bg-white py-3">
        <h2 class="mb-0 h5">회원 정보</h2>
      </div>
      <div class="card-body p-4">
        <form @submit.prevent>
          <div class="row g-3">
            <div class="col-12" v-for="field in fields" :key="field.key">
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
                  :readonly="field.readonly"
                />
              </div>
            </div>
          </div>
          <hr class="my-4" />
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-primary" @click="onEditProfile">
              <i class="bi bi-pencil-square"></i> 회원정보 수정
            </button>
            <button type="button" class="btn btn-outline-secondary" @click="onEditPassword">
              <i class="bi bi-shield-lock"></i> 비밀번호 수정
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, onMounted } from 'vue'
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

const fields = [
  { key: 'email', label: '이메일', icon: 'bi bi-envelope', type: 'email', readonly: true },
  { key: 'name', label: '이름', icon: 'bi bi-person', type: 'text', readonly: true },
  { key: 'birthYear', label: '출생연도', icon: 'bi bi-calendar-event', type: 'text', readonly: true },
  { key: 'gender', label: '성별', icon: 'bi bi-gender-ambiguous', type: 'text', readonly: true },
  { key: 'height', label: '키(cm)', icon: 'bi bi-rulers', type: 'text', readonly: true },
  { key: 'weight', label: '몸무게(kg)', icon: 'bi bi-speedometer2', type: 'text', readonly: true },
  { key: 'activity', label: '활동 정도', icon: 'bi bi-activity', type: 'text', readonly: true },
  { key: 'goal', label: '목표', icon: 'bi bi-bullseye', type: 'text', readonly: true },
]

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

function onEditProfile() {
  router.push({ name: 'updateProfile', params: { userid: form.userId || 'me' } }).catch(() => {})
}

function onEditPassword() {
  router.push({ name: 'updatePassword', params: { userid: form.userId || 'me' } }).catch(() => {})
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
.form-control[readonly] {
  background-color: #fff;
  opacity: 1;
  border-left: 0;
  padding-left: 1rem;
  font-weight: 500;
  color: #212529;
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
</style>

<template>
  <div class="container my-5">
    <div class="card shadow-sm border-light">
      <div class="card-header bg-white py-3">
        <h2 class="mb-0 h5">비밀번호 변경</h2>
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="onSubmit">
          <div class="row g-3">
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="기존 비밀번호">
                  <i class="bi bi-lock"></i>
                </span>
                <input id="currentPw" v-model="form.current" type="password" class="form-control" placeholder="기존 비밀번호" required />
              </div>
            </div>
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="새로운 비밀번호">
                  <i class="bi bi-lock-fill"></i>
                </span>
                <input id="newPw" v-model="form.newPw" type="password" class="form-control" placeholder="새로운 비밀번호" required />
              </div>
            </div>
            <div class="col-12">
              <div class="input-group">
                <span class="input-group-text label-box" title="새로운 비밀번호 다시 확인">
                  <i class="bi bi-lock-fill"></i>
                </span>
                <input id="confirmPw" v-model="form.confirm" type="password" class="form-control" placeholder="새로운 비밀번호 다시 확인" required />
              </div>
            </div>
          </div>
          <p class="mt-2 mb-0" :class="{'text-danger': isMismatch, 'text-success': isMatch}" v-show="isMismatch || isMatch">
            <template v-if="isMismatch">새 비밀번호가 일치하지 않습니다.</template>
            <template v-else-if="isMatch">새 비밀번호가 일치합니다.</template>
          </p>

          <hr class="my-4" />
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="goDetail">
              <i class="bi bi-x-lg"></i> 취소
            </button>
            <button type="submit" class="btn btn-primary">
              <i class="bi bi-check-lg"></i> 변경
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  current: '',
  newPw: '',
  confirm: '',
})

const isMatch = computed(() => form.newPw && form.confirm && form.newPw === form.confirm)
const isMismatch = computed(() => form.newPw && form.confirm && form.newPw !== form.confirm)

async function onSubmit() {
  if (isMismatch.value) {
    alert('새 비밀번호가 일치하지 않습니다.')
    return
  }
  await userStore.updatePassword({
    currentPassword: form.current,
    newPassword: form.newPw,
  })
  goDetail()
}

function goDetail() {
  router.push({ name: 'userDetail', params: { userid: route.params.userid || 'me' } }).catch(() => {})
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
/* Custom styling for hint messages */
.text-danger {
  color: var(--bs-danger) !important;
}
.text-success {
  color: var(--bs-success) !important;
}
</style>

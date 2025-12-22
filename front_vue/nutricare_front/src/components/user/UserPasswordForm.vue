<template>
  <div class="profile-page-wrapper">
    <div class="unified-profile-card">
      <!-- Left Sidebar (Summary) -->
      <aside class="profile-side">
        <div class="avatar-circle">
          {{ userInitial }}
        </div>
        <h1 class="user-name">{{ userStore.userInfo?.name || '사용자' }}</h1>
        <p class="user-email">{{ userStore.userInfo?.email || '이메일 정보 없음' }}</p>
        
        <div class="side-actions">
          <button class="action-btn secondary" @click="goDetail">
            취소 / 돌아가기
          </button>
        </div>
      </aside>

      <!-- Right Main (Password Form) -->
      <main class="info-side">
        <form @submit.prevent="onSubmit">
          <section class="info-group">
            <h3 class="group-title">비밀번호 변경</h3>
            <div class="form-stack">
              <div class="form-item">
                <label for="currentPw" class="label">현재 비밀번호</label>
                <input 
                  id="currentPw" 
                  v-model="form.current" 
                  type="password" 
                  class="input-field" 
                  placeholder="현재 사용 중인 비밀번호" 
                  required 
                />
              </div>

              <div class="form-item">
                <label for="newPw" class="label">새 비밀번호</label>
                <input 
                  id="newPw" 
                  v-model="form.newPw" 
                  type="password" 
                  class="input-field" 
                  placeholder="새로운 비밀번호 입력" 
                  required 
                />
              </div>

              <div class="form-item">
                <label for="confirmPw" class="label">새 비밀번호 확인</label>
                <input 
                  id="confirmPw" 
                  v-model="form.confirm" 
                  type="password" 
                  class="input-field" 
                  placeholder="새로운 비밀번호 재입력" 
                  required 
                />
                
                <!-- Validation Message -->
                <p class="msg-text error" v-if="isMismatch">
                  ⚠️ 새 비밀번호가 일치하지 않습니다.
                </p>
                <p class="msg-text success" v-else-if="isMatch">
                  ✅ 비밀번호가 일치합니다.
                </p>
              </div>
            </div>
          </section>

          <div class="form-actions">
            <button type="submit" class="save-btn">변경하기</button>
          </div>
        </form>
      </main>
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

const userInitial = computed(() => {
  const name = userStore.userInfo?.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const isMatch = computed(() => form.newPw && form.confirm && form.newPw === form.confirm)
const isMismatch = computed(() => form.newPw && form.confirm && form.newPw !== form.confirm)

async function onSubmit() {
  if (isMismatch.value) {
    alert('새 비밀번호가 일치하지 않습니다.')
    return
  }
  try {
    await userStore.updatePassword({
      currentPassword: form.current,
      newPassword: form.newPw,
    })
    alert('비밀번호가 성공적으로 변경되었습니다.')
    goDetail()
  } catch (e) {
    alert('비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.')
  }
}

function goDetail() {
  router.push({ name: 'userDetail', params: { userid: route.params.userid || 'me' } }).catch(() => {})
}
</script>

<style scoped>
/* Unified Layout Styles (Shared with Detail/Update Profile) */
.profile-page-wrapper {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: transparent;
  min-height: 60vh;
}

.unified-profile-card {
  display: flex;
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.08);
  overflow: hidden;
  border: 1px solid #f0f0f0;
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
  margin-bottom: 24px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 400px; /* 비밀번호 폼은 너무 넓지 않게 */
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
}

.input-field {
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  color: #2d3748;
  outline: none;
  transition: border-color 0.2s;
  background-color: #fff;
}

.input-field:focus {
  border-color: #6b55c7;
  box-shadow: 0 0 0 3px rgba(107, 85, 199, 0.1);
}

.msg-text {
  font-size: 13px;
  font-weight: 500;
  margin-top: 4px;
}

.msg-text.error {
  color: #e53e3e;
}

.msg-text.success {
  color: #38a169;
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: flex-start; /* 왼쪽 정렬 혹은 space-between */
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
    max-width: 400px;
  }
  
  .profile-side {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #edf2f7;
    padding: 32px;
  }
  
  .info-side {
    padding: 32px;
  }
  
  .form-stack {
    max-width: 100%;
  }
}
</style>

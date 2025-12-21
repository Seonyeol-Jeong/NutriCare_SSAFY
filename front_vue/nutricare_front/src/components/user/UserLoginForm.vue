<template>
  <div class="form-container">
    <h2 class="title">로그인</h2>
    <p class="subtitle">NutriCare에 오신 것을 환영합니다.</p>
    
    <form @submit.prevent="onLogin" class="form">
      <div class="form-group">
        <label for="email">이메일</label>
        <input 
          id="email" 
          v-model="email" 
          type="email" 
          placeholder="email@example.com" 
          required 
          autocomplete="email"
        />
      </div>
      
      <div class="form-group">
        <label for="password">비밀번호</label>
        <input 
          id="password" 
          v-model="password" 
          type="password" 
          placeholder="비밀번호를 입력하세요" 
          required 
          autocomplete="current-password"
        />
      </div>
      
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      
      <button type="submit" class="submit-button" :disabled="isLoading">
        {{ isLoading ? '로그인 중...' : '로그인' }}
      </button>
    </form>
    <div class="social-login">
      <button @click="googleLogin" class="google-btn">
        <img src="@/assets/google_logo.png" alt="Google" width="20" /> 구글 계정으로 로그인
      </button>
    </div>
    
    <div class="form-footer">
      <p>계정이 없으신가요? <router-link :to="{ name: 'userJoin' }">회원가입</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');
const googleLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
};

const onLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = "이메일과 비밀번호를 모두 입력해주세요.";
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    await userStore.login(email.value, password.value);
    router.replace('/'); 
  } catch (error) {
    console.error('로그인 에러:', error);
    errorMessage.value = '이메일 또는 비밀번호가 올바르지 않습니다.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.form-container {
  width: 100%;
  max-width: 400px;
  margin: auto;
  padding: 40px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
}

.form-group input {
  padding: 12px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #6b55c7;
  box-shadow: 0 0 0 3px rgba(107, 85, 199, 0.1);
}

.error-message {
  color: #e74c3c;
  font-size: 14px;
  text-align: center;
  margin-top: -8px;
  margin-bottom: 8px;
}

.submit-button {
  padding: 14px;
  background: #6b55c7;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 12px;
}

.submit-button:hover:not(:disabled) {
  background-color: #5a45b0;
}

.submit-button:disabled {
  background-color: #c5bada;
  cursor: not-allowed;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #555;
}

.form-footer a {
  color: #6b55c7;
  font-weight: 600;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}

.social-login {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.google-btn {
  width: 100%;
  background-color: #ffffff;
  color: #555;
  border: 1px solid #ddd;
  padding: 14px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  transition: background-color 0.2s, box-shadow 0.2s;
}

.google-btn:hover {
  background-color: #f8f9fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>

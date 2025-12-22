<template>
  <div class="form-container">
    <h2 class="title">회원가입</h2>
    <p class="subtitle">몇 가지 정보만 입력하면 바로 시작할 수 있어요.</p>
    
    <form @submit.prevent="onSignup" class="form">
      <div class="form-group">
        <label for="name">이름</label>
        <input id="name" v-model="form.name" type="text" placeholder="이름을 입력하세요" required />
      </div>

      <div class="form-group">
        <label for="email">이메일</label>
        <input id="email" v-model="form.email" type="email" placeholder="email@example.com" required />
      </div>

      <div class="form-group">
        <label for="password">비밀번호</label>
        <input id="password" v-model="form.password" type="password" placeholder="비밀번호" required />
      </div>

      <div class="form-group">
        <label for="passwordConfirm">비밀번호 확인</label>
        <input id="passwordConfirm" v-model="form.passwordConfirm" type="password" placeholder="비밀번호를 다시 입력하세요" required />
      </div>

      <div class="form-group">
        <label for="birthYear">출생연도</label>
        <input id="birthYear" v-model.number="form.birthYear" type="number" placeholder="YYYY (예: 1998)" />
      </div>

      <div class="form-group">
        <label>성별</label>
        <div class="radio-group">
          <label class="radio-label">
            <input type="radio" v-model="form.gender" value="MALE" />
            <span>남성</span>
          </label>
          <label class="radio-label">
            <input type="radio" v-model="form.gender" value="FEMALE" />
            <span>여성</span>
          </label>
           <label class="radio-label">
            <input type="radio" v-model="form.gender" value="OTHER" />
            <span>기타</span>
          </label>
        </div>
      </div>
      
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      
      <button type="submit" class="submit-button" :disabled="isLoading">
        {{ isLoading ? '가입하는 중...' : '가입하기' }}
      </button>
    </form>
    
    <div class="form-footer">
      <p>이미 계정이 있으신가요? <router-link :to="{ name: 'userLogin' }">로그인</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();

const form = reactive({
  name: '',
  email: '',
  password: '',
  passwordConfirm: '',
  birthYear: null,
  gender: 'MALE',
});

const isLoading = ref(false);
const errorMessage = ref('');

const onSignup = async () => {
  errorMessage.value = '';

  // 클라이언트 측 유효성 검사
  if (form.password !== form.passwordConfirm) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  if (!form.name || !form.email || !form.password) {
    errorMessage.value = '이름, 이메일, 비밀번호는 필수 항목입니다.';
    return;
  }

  isLoading.value = true;
  try {
    const signupData = {
      name: form.name,
      email: form.email.trim(), // 공백 제거 추가
      passwordHash: form.password,
      birthYear: form.birthYear,
      gender: form.gender,
    };
    await userStore.signup(signupData);
    
    // 회원가입 성공
    alert('회원가입에 성공했습니다! 로그인 페이지로 이동합니다.');
    router.push({ name: 'userLogin' });

  } catch (error) {
    console.error('회원가입 에러:', error);
    if (error.response && error.response.status === 409) { // 예시: 이메일 중복 에러
        errorMessage.value = '이미 사용 중인 이메일입니다.';
    } else {
        errorMessage.value = '회원가입에 실패했습니다. 다시 시도해주세요.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 로그인 폼과 동일한 스타일 사용 */
.form-container {
  width: 100%;
  max-width: 420px;
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

.radio-group {
  display: flex;
  gap: 16px;
  padding: 8px 0;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  cursor: pointer;
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
</style>
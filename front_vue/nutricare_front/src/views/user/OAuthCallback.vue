<template>
  <div class="callback-container">
    <div class="loading-spinner"></div>
    <h2>로그인 처리 중입니다...</h2>
    <p>잠시만 기다려주세요.</p>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user'; // 유저 스토어 사용 (선택사항)

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

onMounted(async () => {
  // 1. URL 쿼리 파라미터에서 'token'을 꺼냅니다.
  const token = route.query.token;

  if (token) {
    console.log("구글 로그인 성공! 토큰:", token);

    // 2. 토큰을 로컬 스토리지에 저장합니다.
    // 기존 로직(user.js, axios.js)과 통일하기 위해 'accessToken' 키를 사용합니다.
    localStorage.setItem('accessToken', token);

    // 3. Pinia 스토어 상태 업데이트
    userStore.token = token;
    userStore.isLoggedIn = true;

    // 4. 사용자 정보 가져오기
    try {
      await userStore.fetchMe();
    } catch (error) {
      console.error('사용자 정보 로드 실패:', error);
    }

    // 5. 메인 페이지로 이동합니다.
    router.replace('/'); 
  } else {
    // 토큰이 없다면 에러 상황입니다.
    alert("로그인에 실패했습니다.");
    router.replace('/user/login');
  }
});
</script>

<style scoped>
.callback-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 80vh;
  text-align: center;
}
.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #6b55c7;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
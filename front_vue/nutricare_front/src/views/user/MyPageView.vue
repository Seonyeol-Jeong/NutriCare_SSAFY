<template>
  <div class="page">
    <Header />
    <main class="content">
      <div class="action-stack">
        <button type="button" class="action" @click="goUserDetail">내 정보 보기</button>
        <button type="button" class="action" @click="goUpdateProfile">계정 수정</button>
        <button type="button" class="action" @click="goUpdatePassword">비밀번호 변경</button>
        <button type="button" class="action" @click="goMyBoardList">내 게시글 목록</button>
        <button type="button" class="action" @click="goAnalysisList">내 분석 내역</button>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'

const router = useRouter()
const userStore = useUserStore()
const userId = () => userStore.userId || 'me'

const goUserDetail = () => {
  router.push({ name: 'userDetail', params: { userid: userId() } }).catch(() => {})
}

const goUpdateProfile = () => {
  router.push({ name: 'updateProfile', params: { userid: userId() } }).catch(() => {})
}

const goUpdatePassword = () => {
  router.push({ name: 'updatePassword', params: { userid: userId() } }).catch(() => {})
}

const goMyBoardList = () => {
  router.push({ name: 'myBoardList' }).catch(() => {})
}

const goAnalysisList = () => {
  router.push({ name: 'analysisList', params: { userId: userId() } }).catch(() => {})
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 24px 16px;
}

.action-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 420px;
  margin: 0 auto;
  padding: 50px 0;
}

.action {
  width: 100%;
  padding: 20px 14px;
  background: #ece4ff;
  color: #5a45b0;
  border: 1px solid #d7cfff;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  font-weight: 600;
  font-size: 16px;
  text-align: center;
  margin-bottom: 12px;
}

.action:hover {
  background: #e2d8ff;
}
</style>

<template>
  <div class="page">
    <Header />
    
    <main class="content">
      <div class="dashboard-container">
        <!-- 상단 프로필 섹션 -->
        <section class="profile-card">
          <div class="profile-header">
            <div class="avatar-circle">
              <span class="avatar-text">{{ userInitial }}</span>
            </div>
            <div class="user-info">
              <h2 class="username">{{ userInfo?.name || '사용자' }}</h2>
              <p class="email">{{ userInfo?.email || '이메일 정보 없음' }}</p>
            </div>
          </div>
          
          <div class="health-summary" v-if="healthProfile || userInfo">
            <div class="stat-item">
              <span class="stat-label">성별</span>
              <span class="stat-value">{{ formatGender(userInfo?.gender) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">키</span>
              <span class="stat-value">{{ getHeight(healthProfile) }}cm</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">체중</span>
              <span class="stat-value">{{ getWeight(healthProfile) }}kg</span>
            </div>
          </div>
          <div class="health-summary empty" v-else>
            <p>건강 정보를 등록하고 맞춤 분석을 받아보세요!</p>
          </div>
        </section>

        <!-- 메뉴 그리드 -->
        <section class="menu-grid">
          <div class="menu-card" @click="goAnalysisList">
            <div class="icon-circle analysis-icon">📊</div>
            <div class="menu-text">
              <h3>나의 분석 리포트</h3>
              <p>AI 피부/식단 분석 내역 확인</p>
            </div>
            <span class="arrow-icon">→</span>
          </div>

          <div class="menu-card" @click="goMyBoardList">
            <div class="icon-circle community-icon">✍️</div>
            <div class="menu-text">
              <h3>나의 활동</h3>
              <p>작성한 게시글 및 댓글 관리</p>
            </div>
            <span class="arrow-icon">→</span>
          </div>

          <div class="menu-card" @click="goUserDetail">
            <div class="icon-circle profile-icon">👤</div>
            <div class="menu-text">
              <h3>내 정보 관리</h3>
              <p>상세 정보 조회 및 수정</p>
            </div>
            <span class="arrow-icon">→</span>
          </div>

          <div class="menu-card" @click="goUpdatePassword">
            <div class="icon-circle security-icon">🔒</div>
            <div class="menu-text">
              <h3>보안 설정</h3>
              <p>비밀번호 변경</p>
            </div>
            <span class="arrow-icon">→</span>
          </div>
        </section>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'

const router = useRouter()
const userStore = useUserStore()
const { userInfo, healthProfile } = storeToRefs(userStore)

const userId = () => userStore.userId || 'me'

// 정보가 없거나 오래되었을 수 있으므로 무조건 최신 정보 로드
onMounted(() => {
  userStore.fetchMe()
})

const userInitial = computed(() => {
  const name = userInfo.value?.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

function formatGender(g) {
  if (g === 'M' || g === 'MALE') return '남성'
  if (g === 'F' || g === 'FEMALE') return '여성'
  return '-'
}

// 헬퍼 함수: 다양한 필드명(height vs heightCm) 대응
function getHeight(hp) {
  if (!hp) return '-'
  return hp.heightCm ?? hp.height ?? '-'
}

function getWeight(hp) {
  if (!hp) return '-'
  return hp.weightKg ?? hp.weight ?? '-'
}

const goUserDetail = () => {
  router.push({ name: 'userDetail', params: { userid: userId() } }).catch(() => {})
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
  background-color: #f5f7fa;
}

.content {
  flex: 1;
  padding: 40px 20px;
}

.dashboard-container {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 프로필 카드 스타일 */
.profile-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6b55c7, #9b72e0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(107, 85, 199, 0.3);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 24px;
  font-weight: 800;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.email {
  font-size: 15px;
  color: #8898aa;
  margin: 0;
}

.health-summary {
  display: flex;
  background-color: #f8f9fe;
  border-radius: 12px;
  padding: 20px;
  justify-content: space-around;
}

.health-summary.empty {
  justify-content: center;
  color: #8898aa;
  font-size: 14px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #8898aa;
  font-weight: 600;
  text-transform: uppercase;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #5a45b0;
}

/* 메뉴 그리드 스타일 */
.menu-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.menu-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  border: 1px solid transparent;
}

.menu-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
  border-color: #eaddff;
}

.icon-circle {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.analysis-icon { background-color: #e3f2fd; color: #1e88e5; }
.community-icon { background-color: #f3e5f5; color: #8e24aa; }
.profile-icon { background-color: #e8f5e9; color: #43a047; }
.security-icon { background-color: #fff3e0; color: #fb8c00; }

.menu-text {
  flex: 1;
}

.menu-text h3 {
  font-size: 17px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #333;
}

.menu-text p {
  font-size: 13px;
  color: #8898aa;
  margin: 0;
}

.arrow-icon {
  color: #cbd5e0;
  font-weight: 700;
  font-size: 20px;
  transition: transform 0.2s;
}

.menu-card:hover .arrow-icon {
  color: #5a45b0;
  transform: translateX(4px);
}

@media (min-width: 600px) {
  .menu-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>

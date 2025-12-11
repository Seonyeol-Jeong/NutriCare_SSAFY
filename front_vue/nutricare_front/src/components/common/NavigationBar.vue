<template>
  <header class="nav-bar">
    <div class="nav-inner">
      <button class="logo" type="button" @click="goTo('Home')">
        <span class="logo-mark">로고</span>
      </button>

      <nav class="nav-menu" aria-label="메인 메뉴">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="nav-link"
          type="button"
          @click="goTo(item.routeName, item.routeOptions)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="nav-actions">
        <template v-if="!userStore.isLoggedIn">
          <button class="action-btn" type="button" @click="goTo('login')">로그인</button>
          <button class="action-btn primary" type="button" @click="goTo('signup')">회원가입</button>
        </template>
        <template v-else>
          <button class="profile" type="button" @click="goTo('mypage')">
            <span class="avatar" aria-hidden="true">👤</span>
            <span class="user-name">{{ userStore.userInfo?.name }}</span>
          </button>
          <button class="action-btn" type="button" @click="userStore.logout">로그아웃</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const menuItems = computed(() => [
  { key: 'main', label: 'Main', routeName: 'pageDescribe', routeOptions: { path: '/' } },
  { key: 'intro', label: '소개', routeName: 'engineeringDescribe' },
  { key: 'board', label: '게시판', routeName: 'boardList' },
  { key: 'analysis', label: '분석', routeName: 'analysisUpload' },
])

function goTo(name, routeOptions = {}) {
  if (!name) return
  const target = typeof routeOptions === 'object' && Object.keys(routeOptions).length
    ? { name, ...routeOptions }
    : { name }

  router.push(target).catch(() => {})
}
</script>

<style scoped>
.nav-bar {
  width: 100%;
  border: 1px solid #d9d9d9;
  background: #fdfdfd;
  box-sizing: border-box;
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 10px;
  min-width: 80px;
  border: 1px solid #c7c7c7;
  background: #ececec;
  color: #333;
  font-weight: 600;
  cursor: pointer;
}

.logo-mark {
  font-size: 14px;
}

.nav-menu {
  display: flex;
  gap: 12px;
  flex: 1;
  justify-content: flex-start;
}

.nav-link {
  padding: 8px 10px;
  background: transparent;
  border: none;
  color: #444;
  font-size: 14px;
  cursor: pointer;
}

.nav-link:hover {
  color: #111;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  padding: 8px 12px;
  border: 1px solid #cfcfcf;
  background: #f5f5f5;
  color: #444;
  border-radius: 4px;
  cursor: pointer;
}

.action-btn.primary {
  background: #ece4ff;
  border-color: #d7cfff;
  color: #5a45b0;
}

.action-btn:hover {
  background: #ededed;
}

.profile {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border: 1px solid #e1e1e1;
  background: #fafafa;
  border-radius: 18px;
  cursor: pointer;
}

.avatar {
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  color: #555;
}

@media (max-width: 768px) {
  .nav-inner {
    flex-wrap: wrap;
    gap: 10px;
  }

  .nav-menu {
    order: 3;
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
    row-gap: 6px;
  }

  .nav-actions {
    order: 2;
    margin-left: auto;
  }
}
</style>

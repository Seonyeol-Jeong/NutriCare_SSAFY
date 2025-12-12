<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white border-bottom shadow-sm">
    <div class="container-fluid">
      <a class="navbar-brand" href="#" @click.prevent="goTo('pageDescribe', { path: '/' })">
        <img :src="Logo" alt="NutriCare Logo" class="logo-img" />
      </a>
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
        aria-controls="navbarNav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item" v-for="item in menuItems" :key="item.key">
            <a
              class="nav-link"
              href="#"
              @click.prevent="goTo(item.routeName, item.routeOptions)"
              >{{ item.label }}</a
            >
          </li>
        </ul>
        <div class="d-flex align-items-center">
          <template v-if="!userStore.isLoggedIn">
            <button class="btn btn-sm btn-outline-secondary me-2" type="button" @click="goTo('login')">
              로그인
            </button>
            <button class="btn btn-sm btn-primary" type="button" @click="goTo('signup')">
              회원가입
            </button>
          </template>
          <template v-else>
            <div class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle d-flex align-items-center"
                href="#"
                id="navbarDropdown"
                role="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-person-circle fs-4 me-2"></i>
                <span class="user-name">{{ userStore.userInfo?.name }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                <li><a class="dropdown-item" href="#" @click.prevent="goTo('mypage')">마이페이지</a></li>
                <li><hr class="dropdown-divider" /></li>
                <li>
                  <a class="dropdown-item" href="#" @click.prevent="userStore.logout">로그아웃</a>
                </li>
              </ul>
            </div>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Logo from '@/assets/Logo.png'

const router = useRouter()
const userStore = useUserStore()

const menuItems = computed(() => [
  { key: 'intro', label: '소개', routeName: 'engineeringDescribe' },
  { key: 'board', label: '게시판', routeName: 'boardList' },
  { key: 'analysis', label: '분석', routeName: 'analysisUpload' },
])

function goTo(name, routeOptions = {}) {
  if (!name) return
  const target =
    typeof routeOptions === 'object' && Object.keys(routeOptions).length
      ? { name, ...routeOptions }
      : { name }

  router.push(target).catch(() => {})
}
</script>

<style scoped>
.logo-img {
  height: 32px;
  width: auto;
}

.nav-link {
  font-weight: 500;
}

.user-name {
  font-weight: 600;
}

.dropdown-menu {
  min-width: auto;
}
</style>

<template>
  <nav class="custom-navbar">
    <div class="nav-container">
      <!-- Left: Logo Icon -->
      <a class="nav-brand" href="#" @click.prevent="goTo('pageDescribe', { path: '/' })">
        <img :src="Logo" alt="NutriCare Logo" class="brand-logo" />
      </a>

      <!-- Center: Navigation Links (Desktop) -->
      <div class="nav-menu desktop-only">
        <a 
          v-for="item in menuItems" 
          :key="item.key"
          class="nav-item"
          href="#"
          @click.prevent="goTo(item.routeName, item.routeOptions)"
        >
          {{ item.label }}
        </a>
      </div>

      <!-- Right: Auth / User Profile -->
      <div class="nav-right">
        <!-- Logged In -->
        <template v-if="userStore.isLoggedIn">
          <div class="user-dropdown">
            <button class="user-btn" @click="toggleDropdown">
              <div class="user-avatar">{{ userInitial }}</div>
              <span class="user-name desktop-only">{{ userStore.userInfo?.name }}</span>
              <span class="dropdown-icon">▼</span>
            </button>
            
            <div class="dropdown-content" :class="{ show: isDropdownOpen }">
              <a href="#" class="dropdown-item" @click.prevent="goTo('mypage')">마이페이지</a>
              <div class="dropdown-divider"></div>
              <a href="#" class="dropdown-item danger" @click.prevent="logout">로그아웃</a>
            </div>
          </div>
        </template>

        <!-- Guest -->
        <template v-else>
          <button class="auth-btn login" @click="goTo('login')">로그인</button>
          <button class="auth-btn signup" @click="goTo('signup')">회원가입</button>
        </template>

        <!-- Mobile Menu Toggle -->
        <button class="mobile-toggle" @click="toggleMobileMenu">
          <span class="hamburger"></span>
        </button>
      </div>
    </div>

    <!-- Mobile Menu (Overlay) -->
    <div class="mobile-menu" :class="{ open: isMobileMenuOpen }">
      <div class="mobile-menu-header">
        <div class="placeholder"></div> <!-- Balance the header -->
        <button class="close-btn" @click="toggleMobileMenu">✕</button>
      </div>
      <div class="mobile-links">
        <a 
          v-for="item in menuItems" 
          :key="item.key"
          class="mobile-link"
          href="#"
          @click.prevent="goTo(item.routeName, item.routeOptions); toggleMobileMenu()"
        >
          {{ item.label }}
        </a>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Logo from '@/assets/Logo.png'

const router = useRouter()
const userStore = useUserStore()

const menuItems = computed(() => [
  { key: 'intro', label: '소개', routeName: 'engineeringDescribe' },
  { key: 'board', label: '커뮤니티', routeName: 'boardList' },
  { key: 'analysis', label: 'AI 분석', routeName: 'analysisUpload' },
])

const isDropdownOpen = ref(false)
const isMobileMenuOpen = ref(false)

const userInitial = computed(() => {
  return userStore.userInfo?.name ? userStore.userInfo.name.charAt(0).toUpperCase() : 'U'
})

function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value
}

function toggleMobileMenu() {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

function logout() {
  userStore.logout()
  isDropdownOpen.value = false
}

function goTo(name, routeOptions = {}) {
  if (!name) return
  const target =
    typeof routeOptions === 'object' && Object.keys(routeOptions).length
      ? { name, ...routeOptions }
      : { name }

  router.push(target).catch(() => {})
  isDropdownOpen.value = false
}

// Close dropdown when clicking outside
const closeDropdown = (e) => {
  if (!e.target.closest('.user-dropdown')) {
    isDropdownOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', closeDropdown))
onUnmounted(() => document.removeEventListener('click', closeDropdown))
</script>

<style scoped>
.custom-navbar {
  width: 100%;
  height: 80px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.nav-container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

/* Brand */
.brand-logo {
  height: 56px; /* 40px에서 56px로 확대 */
  width: auto;
  transition: transform 0.2s;
  display: block;
}
.brand-logo:hover {
  transform: scale(1.05);
}

/* Menu (Desktop) */
.nav-menu {
  display: flex;
  gap: 32px;
  flex: 1; /* 가용 공간 차지 */
  justify-content: center; /* 메뉴를 중앙으로 정렬 */
}

.nav-item {
  font-size: 16px;
  font-weight: 600;
  color: #4a5568;
  text-decoration: none;
  position: relative;
  padding: 8px 0;
  transition: color 0.2s;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #6b55c7;
  transition: width 0.3s ease;
}

.nav-item:hover {
  color: #6b55c7;
}

.nav-item:hover::after {
  width: 100%;
}

/* Right Section */
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* Auth Buttons */
.auth-btn {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.auth-btn.login {
  background: transparent;
  color: #4a5568;
}
.auth-btn.login:hover {
  color: #6b55c7;
  background: #f3f0ff;
}

.auth-btn.signup {
  background: linear-gradient(135deg, #6b55c7, #8e44ad);
  color: white;
  box-shadow: 0 4px 10px rgba(107, 85, 199, 0.3);
}
.auth-btn.signup:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(107, 85, 199, 0.4);
}

/* User Dropdown */
.user-dropdown {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 24px;
  transition: background 0.2s;
}

.user-btn:hover {
  background: #f7fafc;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #6b55c7;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}

.dropdown-icon {
  font-size: 10px;
  color: #a0aec0;
}

.dropdown-content {
  position: absolute;
  top: 120%;
  right: 0;
  width: 180px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  padding: 8px;
  border: 1px solid #edf2f7;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.2s ease;
}

.dropdown-content.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  color: #4a5568;
  text-decoration: none;
  font-size: 14px;
  border-radius: 8px;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: #f7fafc;
  color: #6b55c7;
}

.dropdown-item.danger:hover {
  background: #fff5f5;
  color: #e53e3e;
}

.dropdown-divider {
  height: 1px;
  background: #edf2f7;
  margin: 4px 0;
}

/* Mobile Toggle */
.mobile-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px;
}

.hamburger {
  display: block;
  width: 24px;
  height: 2px;
  background: #2d3748;
  position: relative;
}
.hamburger::before,
.hamburger::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 2px;
  background: #2d3748;
  left: 0;
}
.hamburger::before { top: -8px; }
.hamburger::after { top: 8px; }

/* Mobile Menu */
.mobile-menu {
  position: fixed;
  top: 0;
  right: -100%;
  width: 280px;
  height: 100vh;
  background: white;
  box-shadow: -5px 0 20px rgba(0,0,0,0.1);
  z-index: 1001;
  transition: right 0.3s ease;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.mobile-menu.open {
  right: 0;
}

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.mobile-logo {
  height: 24px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #a0aec0;
}

.mobile-links {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mobile-link {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  text-decoration: none;
  padding: 8px 0;
  border-bottom: 1px solid #f7fafc;
}

/* Media Queries */
@media (max-width: 992px) {
  .desktop-only {
    display: none !important;
  }
  .mobile-toggle {
    display: block;
  }
  .nav-center-logo {
    display: none;
  }
}
</style>

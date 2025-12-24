import { defineStore } from 'pinia'
import axios from '@/api/axios'
import router from '@/router'

// 사용자 상태 스토어
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || null,
    userId: localStorage.getItem('userId') || null,
    isLoggedIn: !!localStorage.getItem('accessToken'),
    userInfo: {},
    healthProfile: {},
  }),

  getters: {
    userName: (state) => (state.userInfo ? state.userInfo.name : 'Guest'),
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
  },

  actions: {
    async fetchMe() {
      if (!this.token) return
      try {
        // 인터셉터가 헤더에 토큰을 자동으로 넣어줍니다.
        const response = await axios.get('/users/me')

        // 백엔드 UserDetailResponse 구조: { user: {...}, healthProfile: {...} }
        this.userInfo = response.data.user
        this.healthProfile = response.data.healthProfile || {}
        
        // userId 상태 동기화 (새로고침 시 localStorage 이슈 방지)
        if (this.userInfo.userId) {
          this.userId = this.userInfo.userId
          localStorage.setItem('userId', this.userId)
        }

        this.isLoggedIn = true // 정보 로드 성공 시 로그인 확인
      } catch (error) {
        console.error('내 정보 조회 실패 (토큰 만료 등):', error)
        // 토큰이 유효하지 않으면 로그아웃 처리
        this.logout()
      }
    },

    async login(email, password) {
      try {
        // 백엔드 Login API가 @RequestParam을 사용하므로 params로 전달
        const response = await axios.post('/users/login', null, {
          params: { email, password },
        })

        const { token, userId } = response.data

        if (token) {
          // Pinia 상태 업데이트
          this.token = token
          this.userId = userId
          this.isLoggedIn = true

          // LocalStorage에 저장
          localStorage.setItem('accessToken', token)
          localStorage.setItem('userId', userId)

          // 사용자 정보 가져오기
          await this.fetchMe()

          console.log('로그인 성공:', this.userId)
          return true
        } else {
          // 200 OK 응답이지만 토큰이 없는 경우 (논리적 로그인 실패)
          throw new Error('이메일 또는 비밀번호가 올바르지 않습니다.')
        }
      } catch (error) {
        console.error('로그인 요청 실패:', error)
        // axios 에러 또는 위에서 던진 에러를 다시 던져서 컴포넌트에서 처리
        throw error
      }
    },

    async signup(signupData) {
      try {
        // signupData: { email, passwordHash, name, birthYear, gender }
        await axios.post('/users', signupData)
        return true
      } catch (error) {
        console.error('회원가입 요청 실패:', error)
        // 컴포넌트에서 에러를 처리할 수 있도록 throw
        throw error
      }
    },

    // 로그아웃
    logout() {
      this.token = null
      this.userId = null
      this.isLoggedIn = false
      this.userInfo = null
      this.healthProfile = null

      localStorage.removeItem('accessToken')
      localStorage.removeItem('userId')

      router.push('/').catch(() => {})
    },

    async updateProfile(payload) {
      try {
        const response = await axios.patch('/users/me/info', payload)

        // 응답에 최신 사용자/프로필 정보가 오면 반영
        if (response.data?.user) {
          this.userInfo = response.data.user
        }
        if (response.data?.healthProfile) {
          this.healthProfile = response.data.healthProfile
        } else {
          // healthProfile이 안 오면 로컬 상태라도 업데이트
          if (payload.healthProfile) {
            this.healthProfile = { ...(this.healthProfile || {}), ...payload.healthProfile }
          }
          if (payload.user) {
            this.userInfo = { ...(this.userInfo || {}), ...payload.user }
          }
        }
        await this.fetchMe()
        return true
      } catch (error) {
        console.error('프로필 업데이트 실패:', error)
        throw error
      }
    },

    async updatePassword(payload) {
      try {
        // 예시: { currentPassword, newPassword }
        await axios.patch('/users/me/password', payload)
        return true
      } catch (error) {
        console.error('비밀번호 변경 실패:', error)
        throw error
      }
    },

    // 새로고침 시 세션 복구
    restoreSession() {
      const token = localStorage.getItem('accessToken')
      const userId = localStorage.getItem('userId')
      if (!token) return

      this.token = token
      this.userId = userId
      this.isLoggedIn = true

      // 사용자 정보 갱신
      this.fetchMe()
    },
  },
})

import { defineStore } from 'pinia'
import axios from '@/api/axios'
import router from '@/router';

// 간단한 사용자 상태 스토어 (TODO: API 연동)
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || null,
    userId: localStorage.getItem('userId') || null,
    isLoggedIn: !!localStorage.getItem('accessToken'),
    userInfo: {},
    healthProfile: {},
    profile: {},
  }),

  getters: {
    userName: (state) => state.userInfo ? state.userInfo.name : 'Guest',
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
  },


  actions: {
    async fetchMe() {
      if (!this.token) return;
      try {
        // 인터셉터가 헤더에 토큰을 자동으로 넣어줍니다.
        const response = await axios.get('/users/me');
        
        // 백엔드 UserDetailResponse 구조: { user: {...}, healthProfile: {...} }
        this.userInfo = response.data.user;
        this.healthProfile = response.data.healthProfile || {};
        this.profile = response.data.healthProfile || {};
        
        this.isLoggedIn = true; // 정보 로드 성공 시 로그인 확인
      } catch (error) {
        console.error('내 정보 조회 실패 (토큰 만료 등):', error);
        // 토큰이 유효하지 않으면 로그아웃 처리
        this.logout();
      }
    },
    async login(email, password) {
      try {
        // [주의] 백엔드 Login API가 @RequestParam을 사용하므로 params로 전달
        // 만약 백엔드를 @RequestBody로 변경했다면 두 번째 인자에 { email, password } 객체를 넣어야 함
        const response = await axios.post('/users/login', null, {
          params: {
            email: email,
            password: password
          }
        });

        // LoginResponse DTO 구조에 맞춰 데이터 추출
        const { token, userId } = response.data;

        if (token) {
          // (1) Pinia 상태 업데이트
          this.token = token;
          this.userId = userId;
          this.isLoggedIn = true;
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

          // (2) LocalStorage에 저장 (영구 유지)
          localStorage.setItem('accessToken', token);
          localStorage.setItem('userId', userId);
          
          await this.fetchMe(); // 사용자 정보 가져오기

          console.log('로그인 성공:', this.userId);
          return true; // 성공 시 true 반환
        }
      } catch (error) {
        console.error('로그인 요청 실패:', error);
        throw error; // 컴포넌트에서 에러를 처리할 수 있도록 던짐
      }
    },
    // 3. 로그아웃
    logout() {
      this.token = null;
      this.userId = null;
      this.isLoggedIn = false;
      this.userInfo = null;      // 초기화
      this.healthProfile = null; // 초기화
      this.profile = null;       // 초기화
      delete axios.defaults.headers.common['Authorization'];

      localStorage.removeItem('accessToken');
      localStorage.removeItem('userId');
      
      router.push('/'); // 홈페이지로 리다이렉트
    },
    async updateProfile(payload) {
      // TODO: axios.put('/api/user/profile', payload)
      this.profile = { ...this.profile, ...payload }
    },

    // 새로고침 시 세션 복구
    restoreSession() {
      const token = localStorage.getItem('accessToken')
      const userId = localStorage.getItem('userId')
      if (!token) return

      this.token = token
      this.userId = userId
      this.isLoggedIn = true
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

      // 사용자 정보 갱신
      this.fetchMe()
    },
  },
})

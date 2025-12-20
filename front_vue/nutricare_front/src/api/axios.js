import axios from 'axios';
import { useUserStore } from '@/stores/user';

const instance = axios.create({
  baseURL: '/api',
  timeout: 5000,   // 요청 타임아웃 (5초)
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // cross-origin 요청 시 쿠키를 포함시키기 위한 설정
});

// 2. 요청 인터셉터 설정
instance.interceptors.request.use(
  (config) => {
    const publicPaths = [
      { url: '/users', method: 'post' },
      { url: '/users/login', method: 'post' },
    ];

    const requestUrl = config.url || '';
    const requestMethod = (config.method || '').toLowerCase();

    const isPublicPath = publicPaths.some(
      (path) => requestUrl.endsWith(path.url) && requestMethod === path.method
    );

    // 공개 경로가 아닐 경우에만 토큰 추가
    if (!isPublicPath) {
      const token = localStorage.getItem('accessToken');
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    }

    return config; // 변경된 설정으로 요청 계속 진행
  },
  (error) => {
    // 요청 설정 중에 에러가 난 경우
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// 3. 응답 인터셉터 설정
instance.interceptors.response.use(
  (response) => {
    // 2xx 범위의 상태 코드는 이 함수가 트리거 됩니다.
    // 데이터만 바로 반환하고 싶다면 return response.data; 처럼 쓸 수도 있습니다.
    return response; 
  },
  async (error) => {
    // 2xx 외의 상태 코드는 이 함수가 트리거 됩니다.
    const userStore = useUserStore();

    // 에러 응답이 있는 경우
    if (error.response) {
      const status = error.response.status;

      // [401 Unauthorized] 토큰 만료 또는 인증 실패
      // 단, 로그인 시도 중 발생한 401은 로그아웃 처리(홈 리다이렉트)를 하지 않음
      if (status === 401 && !error.config.url.endsWith('/users/login')) {
        console.warn('세션이 만료되었습니다. 다시 로그인해주세요.');
        
        // 1. 스토어의 로그아웃 액션 실행 (상태 초기화)
        userStore.logout(); 
        
        // 2. 로그인 페이지로 강제 이동
        // router.push('/login'); // 혹은 window.location.href = '/login';
      }
      
      // [403 Forbidden] 권한 없음 (ADMIN 전용 페이지 접근 등)
      if (status === 403) {
        // `/users/me` 요청에서 403 발생 시 (주로 토큰 문제),
        // 경고창 없이 세션 만료 처리를 위해 에러를 그대로 반환합니다.
        // fetchMe의 catch 블록에서 로그아웃을 처리하게 됩니다.
        if (error.config.url !== '/users/me') {
          alert('접근 권한이 없습니다.');
        }
      }
    }

    return Promise.reject(error); // 컴포넌트의 catch 블록으로 에러 전달
  }
);

export default instance; // 생성한 인스턴스를 내보냄
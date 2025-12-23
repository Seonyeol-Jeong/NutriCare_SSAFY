import BoardCreate from '@/components/board/BoardCreate.vue'
import BoardDetail from '@/components/board/BoardDetail.vue'
import BoardList from '@/components/board/BoardList.vue'
import BoardUpdate from '@/components/board/BoardUpdate.vue'
import EngineeringDescribe from '@/components/main/EngineeringDescribe.vue'
import PageDescribe from '@/components/main/PageDescribe.vue'
import BoardView from '@/views/board/BoardView.vue'
import HomeView from '@/views/home/HomeView.vue'
import AnalysisView from '@/views/analysis/AnalysisView.vue'
import AnalysisUploadView from '@/views/analysis/AnalysisUploadView.vue'
import AnalysisResultView from '@/views/analysis/AnalysisResultView.vue'
import AnalysisListView from '@/views/analysis/AnalysisListView.vue'
import AnalysisDetailView from '@/views/analysis/AnalysisDetailView.vue'
import AnalysisDateView from '@/views/analysis/AnalysisDateView.vue'
import MyPageView from '@/views/user/MyPageView.vue'
import UserJoinView from '@/views/user/UserJoinView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserDetailView from '@/views/user/UserDetailView.vue'
import UserProfileView from '@/views/user/UserProfileView.vue'
import UserPasswordView from '@/views/user/UserPasswordView.vue'
import MyBoardListView from '@/views/board/MyBoardListView.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import OAuthCallback from '@/views/user/OAuthCallback.vue'
import DiseaseInfoView from '@/views/DiseaseInfoView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/disease-info',
      name: 'diseaseInfo',
      component: DiseaseInfoView,
    },
    {
      path: '/',
      name: 'Home',
      component: HomeView,
      children: [
        {
          path: '',
          name: 'pageDescribe',
          component: PageDescribe,
        },
        {
          path: 'engineeringDescribe',
          name: 'engineeringDescribe',
          component: EngineeringDescribe,
        },
      ],
    },
    {
      path: '/board',
      name: 'board',
      component: BoardView,
      children: [
        {
          path: 'create',
          name: 'boardCreate',
          component: BoardCreate,
        },
        {
          path: 'detail/:id',
          name: 'boardDetail',
          component: BoardDetail,
        },
        {
          path: 'update/:id',
          name: 'boardUpdate',
          component: BoardUpdate,
        },
        {
          path: '',
          name: 'boardList',
          component: BoardList,
        },
      ],
    },
    {
      path: '/analysis',
      name: 'analysis',
      component: AnalysisView,
      children: [
        {
          path: '',
          redirect: { name: 'analysisUpload' },
        },
        {
          path: 'upload',
          name: 'analysisUpload',
          component: AnalysisUploadView,
        },
        {
          path: 'result/:photoId',
          name: 'analysisResult',
          component: AnalysisResultView,
        },
        {
          path: 'list/:userId',
          name: 'analysisList',
          component: AnalysisListView,
        },
        {
          path: 'detail/:photoId',
          name: 'analysisDetail',
          component: AnalysisDetailView,
        },
        {
          path: 'daily/:date',
          name: 'analysisDate',
          component: AnalysisDateView,
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      redirect: { name: 'userLogin' },
    },
    {
      path: '/signup',
      name: 'signup',
      redirect: { name: 'userJoin' },
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
    },
    {
      path: '/user/join',
      name: 'userJoin',
      component: UserJoinView,
    },
    {
      path: '/user/login',
      name: 'userLogin',
      component: UserLoginView,
    },
    {
      path: '/user/detail/:userid',
      name: 'userDetail',
      component: UserDetailView,
    },
    {
      path: '/user/myboards',
      name: 'myBoardList',
      component: MyBoardListView,
    },
    {
      path: '/user/updateProfile/:userid',
      name: 'updateProfile',
      component: UserProfileView,
    },
    {
      path: '/user/updatePassword/:userid',
      name: 'updatePassword',
      component: UserPasswordView,
    },
    {
      path : '/oauth/callback' ,
      name : 'oauthCallback' ,
      component : OAuthCallback,
    },
  ],
})

// 라우팅 전역 가드
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  // 로그인이 필요 없는 페이지 목록
  const publicPages = [
    'Home', 'pageDescribe', 'engineeringDescribe', 
    'board', 'boardList', 'boardDetail', 
    'userLogin', 'login', 
    'userJoin', 'signup',
    'oauthCallback', // OAuth 콜백 페이지 추가
    'diseaseInfo' // 질환 백과
  ]

  const authRequired = !publicPages.includes(to.name)

  // 1. 로그인이 필요한 페이지에 비로그인 상태로 접근 시
  if (authRequired && !isLoggedIn) {
    alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.')
    return next({ name: 'userLogin' })
  }

  // 2. 로그인 상태에서 로그인/회원가입 페이지 접근 시
  if (isLoggedIn && (to.name === 'userLogin' || to.name === 'userJoin')) {
    return next({ name: 'PageDescribe' })
  }

  // 그 외의 경우는 정상 진행
  next()
})


export default router

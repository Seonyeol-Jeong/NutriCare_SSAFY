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
import MyPageView from '@/views/user/MyPageView.vue'
import UserJoinView from '@/views/user/UserJoinView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserDetailView from '@/views/user/UserDetailView.vue'
import UserProfileView from '@/views/user/UserProfileView.vue'
import UserPasswordView from '@/views/user/UserPasswordView.vue'
import MyBoardListView from '@/views/board/MyBoardListView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
    }
  ],
})

export default router

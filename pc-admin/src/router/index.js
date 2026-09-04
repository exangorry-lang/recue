import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'system/user', name: 'UserList', component: () => import('@/views/system/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'system/dept', name: 'DeptList', component: () => import('@/views/system/DeptList.vue'), meta: { title: '组织架构' } },
      { path: 'system/notice', name: 'NoticeList', component: () => import('@/views/system/NoticeList.vue'), meta: { title: '通知公告' } },
      { path: 'system/log', name: 'LogList', component: () => import('@/views/system/LogList.vue'), meta: { title: '系统日志' } },
      { path: 'training/project', name: 'ProjectList', component: () => import('@/views/training/ProjectList.vue'), meta: { title: '训练项目管理' } },
      { path: 'exam/question', name: 'QuestionList', component: () => import('@/views/exam/QuestionList.vue'), meta: { title: '题库管理' } },
      { path: 'exam/session', name: 'SessionList', component: () => import('@/views/exam/SessionList.vue'), meta: { title: '考试场次' } },
      { path: 'team', name: 'TeamList', component: () => import('@/views/team/TeamList.vue'), meta: { title: '团队管理' } },
      { path: 'checkin', name: 'CheckinList', component: () => import('@/views/checkin/CheckinList.vue'), meta: { title: '打卡审核' } },
      { path: 'archive', name: 'ArchiveList', component: () => import('@/views/archive/ArchiveList.vue'), meta: { title: '档案管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

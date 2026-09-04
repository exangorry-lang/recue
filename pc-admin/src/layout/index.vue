<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-main">潜水救生训练</div>
        <div class="logo-sub">管理系统</div>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#00345a"
        text-color="#c8d6e0"
        active-text-color="#ffffff"
        class="menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon><span>首页</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/dept">组织架构</el-menu-item>
          <el-menu-item index="/system/notice">通知公告</el-menu-item>
          <el-menu-item index="/system/log">系统日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="training">
          <template #title><el-icon><Trophy /></el-icon><span>训练管理</span></template>
          <el-menu-item index="/training/project">训练项目</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exam">
          <template #title><el-icon><EditPen /></el-icon><span>考核管理</span></template>
          <el-menu-item index="/exam/question">题库管理</el-menu-item>
          <el-menu-item index="/exam/session">考试场次</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/team"><el-icon><User /></el-icon><span>团队管理</span></el-menu-item>
        <el-menu-item index="/checkin"><el-icon><CircleCheck /></el-icon><span>打卡审核</span></el-menu-item>
        <el-menu-item index="/archive"><el-icon><Folder /></el-icon><span>档案管理</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-slogan">专业实训 · 分级赋能 · 精准救援 · 常备不懈</div>
        <div class="header-right">
          <span class="header-name">{{ userStore.info?.realName }}（{{ roleText }}）</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
      <el-footer class="footer">© 上海救助基地 潜水救生训练管理系统 版权所有</el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const roleText = computed(() => {
  const info = userStore.info
  if (!info) return ''
  if (info.roles && info.roles.includes('SUPER_ADMIN')) return '超级管理员'
  if (info.roles && info.roles.includes('DEPT_LEADER')) return '部门负责人'
  return `${info.level}级救生员`
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #00345a; }
.logo { padding: 20px 16px; color: #fff; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-main { font-size: 20px; font-weight: 700; }
.logo-sub { font-size: 13px; opacity: 0.8; margin-top: 2px; }
.menu { border-right: none; }
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.header-slogan { color: #005A9C; font-weight: 600; letter-spacing: 1px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.header-name { color: #333; }
.main { padding: 20px; }
.footer { background: #fff; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px; }
</style>

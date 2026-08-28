<template>
  <view>
    <view class="hero">
      <text class="hero-title">潜水救生训练</text>
      <text class="hero-slogan">专业实训 · 分级赋能 · 精准救援 · 常备不懈</text>
    </view>

    <view class="card user-card">
      <view class="user-info">
        <text class="user-name">{{ user.realName || '队员' }}</text>
        <text class="user-tag">{{ levelText }}</text>
      </view>
      <text class="user-sub">今日待训 · 待考核 · 团队任务，请从下方入口进入</text>
    </view>

    <view class="grid">
      <view class="grid-item" v-for="m in menus" :key="m.text" @click="go(m)">
        <text class="grid-icon">{{ m.icon }}</text>
        <text class="grid-text">{{ m.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const user = ref(uni.getStorageSync('user') || {})

const levelText = computed(() => {
  if (!user.value) return ''
  if (user.value.roles && user.value.roles.includes('SUPER_ADMIN')) return '超级管理员'
  if (user.value.roles && user.value.roles.includes('DEPT_LEADER')) return '部门负责人'
  return `${user.value.level}级救生员`
})

const menus = [
  { text: '分级训练', icon: '🏊', url: '/pages/training/list' },
  { text: '理论学习', icon: '📚', url: '/pages/study/study' },
  { text: '考核中心', icon: '📝', url: '/pages/exam/exam' },
  { text: '团队训练', icon: '👥', url: '/pages/team/team' },
  { text: '个人档案', icon: '📋', url: '/pages/profile/profile' }
]

const go = (m) => {
  if (m.url.indexOf('tab') > -1) {
    uni.switchTab({ url: m.url })
  } else {
    uni.navigateTo({ url: m.url })
  }
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #005A9C 0%, #0072BD 100%);
  padding: 60rpx 40rpx;
}
.hero-title { font-size: 44rpx; font-weight: 700; color: #fff; display: block; }
.hero-slogan { font-size: 26rpx; color: rgba(255,255,255,0.85); margin-top: 10rpx; display: block; }
.user-card { margin-top: -20rpx; }
.user-info { display: flex; align-items: center; }
.user-name { font-size: 36rpx; font-weight: 600; color: #333; }
.user-tag {
  margin-left: 16rpx;
  background: #cce2ef;
  color: #005A9C;
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.user-sub { font-size: 26rpx; color: #999; margin-top: 12rpx; display: block; }
.grid { display: flex; flex-wrap: wrap; padding: 20rpx; }
.grid-item {
  width: 30%;
  margin: 1.5%;
  background: #fff;
  border-radius: 12rpx;
  padding: 36rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.grid-icon { font-size: 56rpx; }
.grid-text { font-size: 26rpx; color: #333; margin-top: 12rpx; }
</style>

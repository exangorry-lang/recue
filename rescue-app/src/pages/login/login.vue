<template>
  <view class="login-page">
    <view class="login-logo">
      <text class="login-title">潜水救生训练</text>
      <text class="login-sub">上海救助基地 · 分级赋能 · 精准救援</text>
    </view>
    <view class="login-form">
      <input class="login-input" v-model="form.username" placeholder="请输入账号" placeholder-class="ph" />
      <input class="login-input" v-model="form.password" type="password" placeholder="请输入密码" placeholder-class="ph" />
      <button class="login-btn" :loading="loading" @click="submit">登 录</button>
    </view>
    <text class="login-footer">© 上海救助基地 潜水救生训练管理系统 版权所有</text>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login } from '@/api'

const loading = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password, deviceType: 1 })
    uni.setStorageSync('token', res.data.token)
    uni.setStorageSync('user', res.data.user)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 500)
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #005A9C 0%, #0072BD 60%, #0090d6 100%);
  display: flex;
  flex-direction: column;
  padding: 0 60rpx;
}
.login-logo { margin-top: 180rpx; }
.login-title { font-size: 52rpx; font-weight: 700; color: #fff; display: block; }
.login-sub { font-size: 26rpx; color: rgba(255,255,255,0.85); margin-top: 12rpx; display: block; }
.login-form { margin-top: 100rpx; }
.login-input {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx 30rpx;
  margin-bottom: 24rpx;
  font-size: 30rpx;
}
.ph { color: #bbb; }
.login-btn {
  background: #005A9C;
  color: #fff;
  border-radius: 12rpx;
  margin-top: 20rpx;
  font-size: 32rpx;
}
.login-btn::after { border: none; }
.login-footer { margin-top: 60rpx; text-align: center; color: rgba(255,255,255,0.7); font-size: 22rpx; }
</style>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <h1>潜水救生训练管理系统</h1>
        <p>上海救助基地 · 分级赋能 · 精准救援</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="login-footer">© 上海救助基地 潜水救生训练管理系统 版权所有</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const submit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login({ username: form.username, password: form.password, deviceType: 2 })
      userStore.setLogin(res.data.token, res.data.user)
      ElMessage.success('登录成功')
      router.push('/')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #005A9C 0%, #0072BD 60%, #0090d6 100%);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 36px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
}
.login-logo { text-align: center; margin-bottom: 28px; }
.login-logo h1 { font-size: 22px; color: #005A9C; margin-bottom: 8px; }
.login-logo p { font-size: 13px; color: #666; }
.login-btn { width: 100%; }
.login-footer { text-align: center; color: #999; font-size: 12px; margin-top: 24px; }
</style>

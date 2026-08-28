<template>
  <div>
    <div class="card welcome-card">
      <div class="welcome">您好，{{ userStore.info?.realName }}！</div>
      <div class="slogan">专业实训 · 分级赋能 · 精准救援 · 常备不懈</div>
    </div>
    <el-row :gutter="16" class="stats">
      <el-col :span="4" v-for="s in statCards" :key="s.key">
        <div class="card stat-card">
          <div class="stat-value" :style="{ color: s.color }">{{ stats[s.key] ?? 0 }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatsOverview } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const stats = ref({})

const statCards = [
  { key: 'userCount', label: '在册人员', color: '#005A9C' },
  { key: 'trainProjectCount', label: '训练项目', color: '#0072BD' },
  { key: 'questionCount', label: '题库题目', color: '#09B865' },
  { key: 'checkinCount', label: '训练打卡', color: '#FF7D00' },
  { key: 'examScoreCount', label: '考核成绩', color: '#F53F3F' },
  { key: 'teamGroupCount', label: '训练团队', color: '#005A9C' }
]

onMounted(async () => {
  const res = await getStatsOverview()
  stats.value = res.data
})
</script>

<style scoped>
.welcome-card { margin-bottom: 16px; }
.welcome { font-size: 20px; font-weight: 600; color: #333; }
.slogan { font-size: 14px; color: #666; margin-top: 6px; }
.stats { margin-top: 8px; }
.stat-card { text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 13px; color: #666; margin-top: 6px; }
</style>

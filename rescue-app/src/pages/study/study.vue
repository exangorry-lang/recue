<template>
  <view>
    <view class="filter">
      <view class="filter-item" :class="{ active: mode === 'practice' }" @click="switchMode('practice')">刷题练习</view>
      <view class="filter-item" :class="{ active: mode === 'wrong' }" @click="switchMode('wrong')">错题复盘</view>
    </view>

    <view class="filter" v-if="mode === 'practice'">
      <picker :range="[1,2,3,4,5]" @change="onLevelChange">
        <view class="filter-item">{{ level }}级 ▾</view>
      </picker>
      <picker :range="typeList" range-key="label" @change="onTypeChange">
        <view class="filter-item">{{ typeLabel }} ▾</view>
      </picker>
    </view>

    <!-- 错题复盘 -->
    <template v-if="mode === 'wrong'">
      <view class="card" v-for="w in wrongs" :key="w.questionId">
        <view class="q-tag">{{ typeText(w.questionType) }}</view>
        <text class="q-content">{{ w.content }}</text>
        <text class="wrong-my">我的答案：{{ w.myAnswer }}</text>
        <text class="answer-text">正确答案：{{ w.answer }}</text>
        <text class="analysis" v-if="w.analysis">解析：{{ w.analysis }}</text>
      </view>
      <view v-if="!wrongs.length" class="empty">暂无错题，继续保持</view>
    </template>

    <!-- 刷题 -->
    <template v-if="mode === 'practice'">
      <view class="card" v-if="current">
        <view class="q-tag">{{ typeText(current.questionType) }} · {{ current.level }}级 · {{ current.chapter || '未分章' }}</view>
        <text class="q-content">{{ current.content }}</text>

        <!-- 判断题：对/错按钮 -->
        <view v-if="current.questionType === 3" class="options">
          <view class="option" :class="optionClass('对')" @click="pickOption('对')">对</view>
          <view class="option" :class="optionClass('错')" @click="pickOption('错')">错</view>
        </view>
        <!-- 单选/多选：选项列表 -->
        <view v-else-if="current.questionType !== 4 && options.length" class="options">
          <view
            v-for="(opt, idx) in options"
            :key="idx"
            class="option"
            :class="optionClass(opt)"
            @click="pickOption(opt)"
          >{{ opt }}</view>
        </view>
        <!-- 简答：输入框 -->
        <input v-else v-model="selected" class="answer-input" placeholder="请输入你的答案" />

        <!-- 提交答案按钮（未提交时显示） -->
        <button v-if="!submitted" class="check-btn" @click="submitAnswer">提交答案</button>

        <!-- 提交后结果 -->
        <view v-if="submitted" class="result">
          <template v-if="isCorrect === true">
            <text class="answer-text">回答正确 ✓</text>
            <text class="answer-text">正确答案：{{ current.answer }}</text>
          </template>
          <template v-else-if="isCorrect === false">
            <text class="wrong-your">您的选择：{{ displaySelected }}</text>
            <text class="answer-text">正确答案：{{ current.answer }}</text>
          </template>
          <template v-else>
            <text class="answer-text">参考答案：{{ current.answer }}</text>
          </template>
          <text class="analysis" v-if="current.analysis">解析：{{ current.analysis }}</text>
        </view>
      </view>

      <view v-if="!current && !loading" class="empty">该条件下暂无题目</view>

      <view v-if="questions.length" class="nav-row">
        <button class="nav-btn" @click="prev">上一题</button>
        <text class="nav-count">{{ index + 1 }} / {{ questions.length }}</text>
        <button class="nav-btn" @click="next">下一题</button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getQuestionPage, getWrongList } from '@/api'

const mode = ref('practice')
const wrongs = ref([])
const level = ref(1)
const typeList = [
  { label: '全部题型', value: 0 },
  { label: '单选', value: 1 },
  { label: '多选', value: 2 },
  { label: '判断', value: 3 },
  { label: '简答', value: 4 }
]
const type = ref(0)
const questions = ref([])
const index = ref(0)
const loading = ref(false)
const selected = ref('')
const submitted = ref(false)

const typeLabel = computed(() => typeList.find(t => t.value === type.value)?.label || '全部题型')
const current = computed(() => questions.value[index.value] || null)
const options = computed(() => {
  if (!current.value || !current.value.options) return []
  try {
    const arr = JSON.parse(current.value.options)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
})

const optionCode = (opt) => {
  const match = String(opt).trim().match(/^([A-Za-z])[.、．\s]/)
  return match ? match[1].toUpperCase() : String(opt).trim()
}
const normalizedAnswer = (answer) => String(answer || '').toUpperCase().replace(/[\s,，、]/g, '').split('').sort().join('')
const isMultiple = () => current.value?.questionType === 2
const isOptionSelected = (opt) => isMultiple()
  ? selected.value.includes(optionCode(opt))
  : selected.value === optionCode(opt)

const optionClass = (opt) => {
  if (!submitted.value) {
    // 未提交：只显示选中态
    return { selected: isOptionSelected(opt) }
  }
  const code = optionCode(opt)
  const inAnswer = normalizedAnswer(current.value?.answer).includes(code)
  return {
    selected: isOptionSelected(opt),
    correct: inAnswer,
    wrong: isOptionSelected(opt) && !inAnswer
  }
}

const pickOption = (opt) => {
  if (submitted.value) return
  const code = optionCode(opt)
  if (!isMultiple()) {
    selected.value = code
    return
  }
  const values = selected.value.split('').filter(Boolean)
  selected.value = values.includes(code)
    ? values.filter(value => value !== code).join('')
    : [...values, code].sort().join('')
}

const displaySelected = computed(() => selected.value)

const isCorrect = computed(() => {
  if (!current.value || !submitted.value) return null
  if (current.value.questionType === 4) return null
  return normalizedAnswer(selected.value) === normalizedAnswer(current.value.answer)
})

const typeText = (t) => ({ 1: '单选', 2: '多选', 3: '判断', 4: '简答' }[t] || t)

const switchMode = (m) => {
  mode.value = m
  if (m === 'wrong') {
    loadWrong()
  }
}

const loadWrong = async () => {
  const res = await getWrongList()
  wrongs.value = res.data
}

const load = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 50, level: level.value }
    if (type.value) params.questionType = type.value
    const res = await getQuestionPage(params)
    questions.value = res.data.records
    index.value = 0
    selected.value = ''
    submitted.value = false
  } finally {
    loading.value = false
  }
}

const onLevelChange = (e) => {
  level.value = Number(e.detail.value) + 1
  load()
}

const onTypeChange = (e) => {
  type.value = typeList[Number(e.detail.value)].value
  load()
}

const submitAnswer = () => {
  if (!selected.value) {
    uni.showToast({ title: '请先选择答案', icon: 'none' })
    return
  }
  submitted.value = true
}

const prev = () => {
  if (index.value > 0) {
    index.value--
    selected.value = ''
    submitted.value = false
  }
}

const next = () => {
  if (index.value < questions.value.length - 1) {
    index.value++
    selected.value = ''
    submitted.value = false
  }
}

onShow(() => {
  const user = uni.getStorageSync('user')
  if (user && user.level) level.value = user.level
  load()
})
</script>

<style scoped>
.filter { display: flex; background: #fff; padding: 20rpx; }
.filter-item { padding: 12rpx 32rpx; background: #F5F7FA; border-radius: 30rpx; font-size: 28rpx; color: #666; margin-right: 20rpx; }
.filter-item.active { background: #005A9C; color: #fff; }
.q-tag { font-size: 22rpx; color: #005A9C; background: #cce2ef; padding: 4rpx 16rpx; border-radius: 20rpx; display: inline-block; }
.q-content { font-size: 32rpx; color: #333; line-height: 1.6; margin-top: 20rpx; display: block; }
.options { margin-top: 24rpx; }
.option {
  padding: 24rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  color: #333;
}
.option.selected { border-color: #005A9C; background: #cce2ef; }
.option.wrong { border-color: #F53F3F; background: #FFF1F0; color: #C2382B; }
.option.correct { border-color: #09B865; background: #ECF9F1; color: #087A46; }
.answer-input { background: #F5F7FA; border-radius: 12rpx; padding: 20rpx; margin-top: 20rpx; font-size: 28rpx; }
.check-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin-top: 30rpx; }
.check-btn::after { border: none; }
.result { margin-top: 20rpx; }
.answer-text { font-size: 28rpx; color: #09B865; display: block; }
.wrong-your { font-size: 28rpx; color: #F53F3F; display: block; margin-bottom: 4rpx; }
.analysis { font-size: 26rpx; color: #666; margin-top: 8rpx; display: block; line-height: 1.6; }
.wrong-my { font-size: 28rpx; color: #F53F3F; margin-top: 12rpx; display: block; }
.nav-row { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 40rpx; }
.nav-btn { background: #fff; color: #005A9C; border-radius: 30rpx; font-size: 26rpx; padding: 0 40rpx; }
.nav-btn::after { border: none; }
.nav-count { font-size: 26rpx; color: #666; }
.empty { text-align: center; color: #999; padding: 100rpx 0; font-size: 28rpx; }
</style>

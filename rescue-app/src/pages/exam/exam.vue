<template>
  <view>
    <!-- 场次列表 -->
    <view v-if="mode === 'list'">
      <view class="section-title" style="padding: 20rpx 20rpx 0;">考试场次</view>
      <view class="card" v-for="s in sessions" :key="s.id">
        <view class="session-head">
          <text class="session-name">{{ s.name }}</text>
          <text class="session-level">{{ s.level }}级</text>
        </view>
        <text class="session-info">{{ s.examType === 1 ? '模拟自测' : '正式晋升考试' }} · {{ s.duration }}分钟 · 及格 {{ s.passScore }}分</text>
        <button class="start-btn" @click="start(s)">开始考试</button>
      </view>
      <view v-if="!sessions.length" class="empty">暂无考试场次</view>

      <view class="section-title" style="padding: 20rpx 20rpx 0;">我的成绩</view>
      <view class="card" v-for="s in scores" :key="s.id">
        <view class="score-row">
          <text class="score-info">{{ s.level }}级 · 场次{{ s.sessionId }}</text>
          <text class="score-num" :class="s.isPass === 1 ? 'ok' : 'bad'">{{ s.score }}分</text>
        </view>
      </view>
      <view v-if="!scores.length" class="empty">暂无成绩记录</view>
    </view>

    <!-- 逐题答题 -->
    <view v-else-if="mode === 'answering'">
      <view class="progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</view>

      <view class="card" v-if="currentQuestion">
        <text class="q-content">{{ currentIndex + 1 }}. {{ currentQuestion.content }}</text>

        <!-- 判断题 -->
        <view v-if="currentQuestion.questionType === 3" class="options">
          <view class="option" :class="optionClass('对')" @click="pick('对')">对</view>
          <view class="option" :class="optionClass('错')" @click="pick('错')">错</view>
        </view>
        <!-- 单选/多选 -->
        <view v-else-if="currentQuestion.questionType !== 4 && currentOptions.length" class="options">
          <view
            v-for="(opt, idx) in currentOptions"
            :key="idx"
            class="option"
            :class="optionClass(opt)"
            @click="pick(opt)"
          >{{ opt }}</view>
        </view>
        <!-- 简答 -->
        <input v-else v-model="answers[currentQuestion.id]" class="answer-input" placeholder="输入答案" />
      </view>

      <view class="nav-row">
        <button class="nav-btn" @click="prevQuestion">上一题</button>
        <button v-if="currentIndex < questions.length - 1" class="nav-btn primary" @click="nextQuestion">下一题</button>
        <button v-else class="submit-btn" :loading="submitting" @click="submit">交 卷</button>
      </view>
    </view>

    <!-- 结果 + 全部题目对错 -->
    <view v-else class="result-wrap">
      <view class="card result-card">
        <text class="result-score">{{ result.score }}分</text>
        <text class="result-pass" :class="result.hasSubjective ? 'pending' : result.isPass === 1 ? 'ok' : 'bad'">
          {{ result.hasSubjective ? '待人工阅卷' : result.isPass === 1 ? '合格' : '不合格' }}
        </text>
        <text class="result-detail">答对 {{ result.correctCount }} 题{{ result.hasSubjective ? '（含简答题待人工阅卷）' : '' }}</text>
        <button class="back-btn" @click="backToList">返回列表</button>
      </view>

      <view class="section-title" style="padding: 20rpx 20rpx 0;">题目回顾</view>
      <view class="card" v-for="(q, i) in questionResults" :key="q.questionId">
        <text class="q-content">{{ i + 1 }}. {{ q.content }}</text>
        <text v-if="q.userAnswer" class="wrong-your">您的选择：{{ q.userAnswer }}</text>
        <text v-if="q.isCorrect === 1" class="answer-text">回答正确 ✓</text>
        <text v-else-if="q.isCorrect === 0" class="wrong-your">回答错误</text>
        <text v-else class="pending-text">简答题待人工阅卷</text>
        <text class="answer-text">正确答案：{{ q.answer }}</text>
        <text class="analysis" v-if="q.analysis">解析：{{ q.analysis }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSessionList, getMyScore, startExam, submitExam } from '@/api'

const mode = ref('list')
const sessions = ref([])
const scores = ref([])
const questions = ref([])
const answers = ref({})
const recordId = ref(null)
const result = ref({})
const questionResults = ref([])
const currentIndex = ref(0)
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const currentOptions = computed(() => {
  if (!currentQuestion.value || !currentQuestion.value.options) return []
  try {
    const arr = JSON.parse(currentQuestion.value.options)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
})

const load = async () => {
  const user = uni.getStorageSync('user')
  const params = user && user.level ? { level: user.level } : {}
  const [s, sc] = await Promise.all([getSessionList(params), getMyScore()])
  sessions.value = s.data
  scores.value = sc.data
}

const optionCode = (opt) => {
  const match = String(opt).trim().match(/^([A-Za-z])[.、．\s]/)
  return match ? match[1].toUpperCase() : String(opt).trim()
}

const optionClass = (opt) => {
  const q = currentQuestion.value
  if (!q) return {}
  const code = optionCode(opt)
  const answer = String(answers.value[q.id] || '')
  const selected = q.questionType === 2 ? answer.includes(code) : answer === code
  return { selected }
}

const pick = (opt) => {
  const q = currentQuestion.value
  if (!q) return
  const code = optionCode(opt)
  if (q.questionType !== 2) {
    answers.value[q.id] = code
    return
  }
  const values = String(answers.value[q.id] || '').split('').filter(Boolean)
  answers.value[q.id] = values.includes(code)
    ? values.filter(v => v !== code).join('')
    : [...values, code].sort().join('')
}

const prevQuestion = () => {
  if (currentIndex.value > 0) currentIndex.value--
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value++
}

const start = async (s) => {
  try {
    const res = await startExam(s.id)
    recordId.value = res.data.recordId
    questions.value = res.data.questions
    answers.value = {}
    currentIndex.value = 0
    mode.value = 'answering'
  } catch (e) {
    uni.showToast({ title: e.message || '无法开始考试', icon: 'none' })
  }
}

const submit = async () => {
  submitting.value = true
  try {
    const list = Object.keys(answers.value).map(qid => ({
      questionId: Number(qid),
      answer: answers.value[qid]
    }))
    const res = await submitExam(recordId.value, list)
    result.value = res.data
    questionResults.value = res.data.questionResults || []
    mode.value = 'result'
  } catch (e) {
    uni.showToast({ title: e.message || '交卷失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const backToList = () => {
  mode.value = 'list'
  load()
}

onShow(load)
</script>

<style scoped>
.session-head { display: flex; justify-content: space-between; align-items: center; }
.session-name { font-size: 32rpx; font-weight: 600; color: #333; }
.session-level { font-size: 24rpx; color: #005A9C; background: #cce2ef; padding: 4rpx 16rpx; border-radius: 20rpx; }
.session-info { font-size: 26rpx; color: #999; margin-top: 12rpx; display: block; }
.start-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin-top: 20rpx; }
.start-btn::after { border: none; }
.score-row { display: flex; justify-content: space-between; align-items: center; }
.score-info { font-size: 28rpx; color: #666; }
.score-num { font-size: 36rpx; font-weight: 700; }
.score-num.ok { color: #09B865; }
.score-num.bad { color: #F53F3F; }
.progress { text-align: center; color: #666; font-size: 26rpx; padding: 20rpx 0; }
.q-content { font-size: 30rpx; color: #333; line-height: 1.6; display: block; }
.options { margin-top: 20rpx; }
.option { padding: 22rpx; border: 2rpx solid #eee; border-radius: 12rpx; margin-bottom: 14rpx; font-size: 28rpx; color: #333; }
.option.selected { border-color: #005A9C; background: #cce2ef; }
.answer-input { background: #F5F7FA; border-radius: 12rpx; padding: 20rpx; margin-top: 20rpx; font-size: 28rpx; }
.nav-row { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 40rpx; }
.nav-btn { background: #fff; color: #005A9C; border-radius: 30rpx; font-size: 26rpx; padding: 0 40rpx; }
.nav-btn::after { border: none; }
.nav-btn.primary { background: #005A9C; color: #fff; }
.submit-btn { background: #005A9C; color: #fff; border-radius: 30rpx; font-size: 26rpx; padding: 0 50rpx; }
.submit-btn::after { border: none; }
.result-wrap { padding-bottom: 40rpx; }
.result-card { text-align: center; padding: 60rpx 40rpx; }
.result-score { font-size: 80rpx; font-weight: 700; color: #005A9C; display: block; }
.result-pass { font-size: 36rpx; font-weight: 600; margin-top: 16rpx; display: block; }
.result-pass.ok { color: #09B865; }
.result-pass.bad { color: #F53F3F; }
.result-pass.pending { color: #FF7D00; }
.result-detail { font-size: 26rpx; color: #666; margin-top: 16rpx; display: block; }
.back-btn { background: #fff; color: #005A9C; border-radius: 12rpx; margin-top: 30rpx; }
.back-btn::after { border: none; }
.answer-text { font-size: 28rpx; color: #09B865; display: block; }
.wrong-your { font-size: 28rpx; color: #F53F3F; display: block; margin: 8rpx 0; }
.pending-text { font-size: 28rpx; color: #FF7D00; display: block; margin: 8rpx 0; }
.analysis { font-size: 26rpx; color: #666; margin-top: 8rpx; display: block; line-height: 1.6; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>

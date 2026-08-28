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

    <!-- 答题 -->
    <view v-else-if="mode === 'answering'">
      <view class="card" v-for="(q, i) in questions" :key="q.id">
        <text class="q-content">{{ i + 1 }}. {{ q.content }}</text>
        <view v-if="q.questionType !== 4" class="options">
          <view
            v-for="(opt, oi) in parseOptions(q.options)"
            :key="oi"
            class="option"
            :class="{ selected: answers[q.id] === opt }"
            @click="answers[q.id] = opt"
          >{{ opt }}</view>
        </view>
        <input v-else v-model="answers[q.id]" class="answer-input" placeholder="输入答案" />
      </view>
      <button class="submit-btn" :loading="submitting" @click="submit">交 卷</button>
    </view>

    <!-- 结果 -->
    <view v-else class="card result-card">
      <text class="result-score">{{ result.score }}分</text>
      <text class="result-pass" :class="result.isPass === 1 ? 'ok' : 'bad'">{{ result.isPass === 1 ? '合格' : '不合格' }}</text>
      <text class="result-detail">答对 {{ result.correctCount }} 题{{ result.hasSubjective ? '（含简答题待人工阅卷）' : '' }}</text>
      <button class="back-btn" @click="backToList">返回列表</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSessionList, getMyScore, startExam, submitExam } from '@/api'

const mode = ref('list')
const sessions = ref([])
const scores = ref([])
const questions = ref([])
const answers = ref({})
const recordId = ref(null)
const result = ref({})
const submitting = ref(false)

const load = async () => {
  const user = uni.getStorageSync('user')
  const params = user && user.level ? { level: user.level } : {}
  const [s, sc] = await Promise.all([getSessionList(params), getMyScore()])
  sessions.value = s.data
  scores.value = sc.data
}

const parseOptions = (opts) => {
  if (!opts) return []
  try {
    const arr = JSON.parse(opts)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}

const start = async (s) => {
  try {
    const res = await startExam(s.id)
    recordId.value = res.data.recordId
    questions.value = res.data.questions
    answers.value = {}
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
.q-content { font-size: 30rpx; color: #333; line-height: 1.6; display: block; }
.options { margin-top: 20rpx; }
.option { padding: 22rpx; border: 2rpx solid #eee; border-radius: 12rpx; margin-bottom: 14rpx; font-size: 28rpx; color: #333; }
.option.selected { border-color: #005A9C; background: #cce2ef; }
.answer-input { background: #F5F7FA; border-radius: 12rpx; padding: 20rpx; margin-top: 20rpx; font-size: 28rpx; }
.submit-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin: 30rpx 20rpx; }
.submit-btn::after { border: none; }
.result-card { text-align: center; padding: 80rpx 40rpx; }
.result-score { font-size: 80rpx; font-weight: 700; color: #005A9C; display: block; }
.result-pass { font-size: 36rpx; font-weight: 600; margin-top: 16rpx; display: block; }
.result-pass.ok { color: #09B865; }
.result-pass.bad { color: #F53F3F; }
.result-detail { font-size: 26rpx; color: #666; margin-top: 16rpx; display: block; }
.back-btn { background: #fff; color: #005A9C; border-radius: 12rpx; margin-top: 40rpx; }
.back-btn::after { border: none; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>

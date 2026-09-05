<template>
  <view>
    <view class="card" v-if="project">
      <text class="detail-title">{{ project.name }}</text>
      <text class="detail-cat">{{ project.level }}级 · {{ project.category || '未分类' }}</text>

      <view class="detail-section" v-if="project.outline">
        <text class="label">训练大纲</text>
        <text class="content">{{ project.outline }}</text>
      </view>
      <view class="detail-section" v-if="project.steps">
        <text class="label">实操步骤</text>
        <text class="content">{{ project.steps }}</text>
      </view>
      <view class="detail-section" v-if="project.standard">
        <text class="label">标准要求</text>
        <text class="content">{{ project.standard }}</text>
      </view>
      <view class="detail-section" v-if="project.tips">
        <text class="label">易错提示</text>
        <text class="content">{{ project.tips }}</text>
      </view>
    </view>

    <view class="checkin-panel" v-if="project">
      <text class="section-title">实训打卡</text>

      <view v-if="myCheckins.length" class="my-checkins">
        <text class="sub-title">我的打卡记录</text>
        <view v-for="c in myCheckins" :key="c.id" class="checkin-item">
          <text class="checkin-time">{{ c.checkinTime }}</text>
          <text class="checkin-status" :class="'st-' + c.status">{{ statusText(c.status) }}</text>
        </view>
      </view>

      <template v-if="hasPending">
        <view class="pending-tip">该训练已有待审核的打卡，请等待审核结果</view>
      </template>
      <template v-else>
        <view class="eval-row">
          <text class="eval-label">自主自评</text>
          <view class="eval-opts">
            <view
              v-for="e in evals"
              :key="e.value"
              class="eval-opt"
              :class="{ selected: selfEval === e.value }"
              @click="selfEval = e.value"
            >{{ e.label }}</view>
          </view>
        </view>
        <input class="comment-input" v-model="selfComment" placeholder="自评说明（可选）" />
        <button class="checkin-btn" :loading="submitting" @click="doCheckin">提交打卡</button>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProjectDetail, submitCheckin, getMyProjectCheckins } from '@/api'

const project = ref(null)
const projectId = ref(null)
const selfEval = ref(1)
const selfComment = ref('')
const submitting = ref(false)
const myCheckins = ref([])
const evals = [
  { label: '合格', value: 1 },
  { label: '基本合格', value: 2 },
  { label: '不合格', value: 3 }
]

const hasPending = computed(() => myCheckins.value.some(c => c.status === 0))

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已驳回' }[s] || s)

const loadCheckins = async () => {
  const res = await getMyProjectCheckins(projectId.value)
  myCheckins.value = res.data || []
}

onLoad(async (option) => {
  projectId.value = Number(option.id)
  const res = await getProjectDetail(option.id)
  project.value = res.data.project
  loadCheckins()
})

const doCheckin = async () => {
  submitting.value = true
  try {
    await submitCheckin({
      projectId: project.value.id,
      selfEval: selfEval.value,
      selfComment: selfComment.value,
      progress: 100,
      offlineFlag: 0
    })
    uni.showToast({ title: '打卡已提交，待审核', icon: 'success' })
    selfComment.value = ''
    loadCheckins()
  } catch (e) {
    uni.showToast({ title: e.message || '打卡失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.detail-title { font-size: 36rpx; font-weight: 700; color: #333; display: block; }
.detail-cat { font-size: 24rpx; color: #005A9C; margin-top: 8rpx; display: block; }
.detail-section { margin-top: 24rpx; }
.label { font-size: 28rpx; font-weight: 600; color: #005A9C; display: block; }
.content { font-size: 28rpx; color: #333; line-height: 1.7; margin-top: 8rpx; display: block; white-space: pre-wrap; }
.checkin-panel { margin-top: 20rpx; }
.eval-row { display: flex; align-items: center; margin-top: 16rpx; }
.eval-label { font-size: 28rpx; color: #666; }
.eval-opts { display: flex; margin-left: 20rpx; }
.eval-opt {
  padding: 10rpx 28rpx;
  margin-right: 16rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;
  background: #F5F7FA;
}
.eval-opt.selected { background: #005A9C; color: #fff; }
.comment-input {
  background: #F5F7FA;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-top: 20rpx;
  font-size: 28rpx;
}
.checkin-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin-top: 30rpx; }
.checkin-btn::after { border: none; }
.my-checkins { margin-top: 16rpx; }
.sub-title { font-size: 28rpx; font-weight: 600; color: #333; display: block; margin-bottom: 8rpx; }
.checkin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.checkin-time { font-size: 26rpx; color: #666; }
.checkin-status { font-size: 26rpx; font-weight: 600; }
.st-0 { color: #FF7D00; }
.st-1 { color: #09B865; }
.st-2 { color: #F53F3F; }
.pending-tip {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #FFF4E5;
  color: #FF7D00;
  font-size: 26rpx;
  border-radius: 12rpx;
}
</style>

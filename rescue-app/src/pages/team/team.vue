<template>
  <view>
    <view class="section-title" style="padding: 20rpx 20rpx 0;">我的班组</view>
    <view class="card" v-for="g in groups" :key="g.id">
      <text class="group-name">{{ g.name }}</text>
      <text class="group-sub">{{ g.remark || '训练团队' }}</text>
    </view>
    <view v-if="!groups.length" class="empty">尚未加入班组</view>

    <view class="section-title" style="padding: 20rpx 20rpx 0;">团队任务</view>
    <view class="card" v-for="t in tasks" :key="t.id">
      <view class="task-head">
        <text class="task-name">{{ t.name }}</text>
        <text class="task-type">{{ t.taskType === 1 ? '日常实训' : '救援演练' }}</text>
      </view>
      <text class="task-content" v-if="t.content">{{ t.content }}</text>
      <button
        class="checkin-btn"
        :class="{ done: joinedTaskIds.includes(t.id) }"
        :disabled="joinedTaskIds.includes(t.id)"
        @click="checkin(t)"
      >{{ joinedTaskIds.includes(t.id) ? '已参与' : '参与打卡' }}</button>
    </view>
    <view v-if="!tasks.length" class="empty">暂无团队任务</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyGroups, getTaskList, submitTeamRecord, getMyTeamRecords } from '@/api'

const groups = ref([])
const tasks = ref([])
const joinedTaskIds = ref([])

const load = async () => {
  const [g, t, r] = await Promise.all([getMyGroups(), getTaskList(), getMyTeamRecords()])
  groups.value = g.data
  tasks.value = t.data
  joinedTaskIds.value = (r.data || []).map(x => x.taskId)
}

const checkin = async (t) => {
  try {
    await submitTeamRecord({ taskId: t.id, status: 1, offlineFlag: 0 })
    joinedTaskIds.value.push(t.id)
    uni.showToast({ title: '打卡成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '打卡失败', icon: 'none' })
  }
}

onShow(load)
</script>

<style scoped>
.group-name { font-size: 32rpx; font-weight: 600; color: #333; display: block; }
.group-sub { font-size: 26rpx; color: #999; margin-top: 8rpx; display: block; }
.task-head { display: flex; justify-content: space-between; align-items: center; }
.task-name { font-size: 32rpx; font-weight: 600; color: #333; }
.task-type { font-size: 24rpx; color: #005A9C; background: #cce2ef; padding: 4rpx 16rpx; border-radius: 20rpx; }
.task-content { font-size: 26rpx; color: #666; margin-top: 12rpx; display: block; line-height: 1.6; }
.checkin-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin-top: 20rpx; }
.checkin-btn::after { border: none; }
.checkin-btn.done { background: #CCCCCC; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>

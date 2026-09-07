<template>
  <view>
    <view class="card profile-card">
      <view class="avatar">{{ (user.realName || '救')[0] }}</view>
      <text class="profile-name">{{ user.realName || '队员' }}</text>
      <text class="profile-level">{{ levelText }}</text>
    </view>

    <view class="card">
      <text class="section-title">档案信息</text>
      <view class="info-row">
        <text class="info-label">当前等级</text>
        <text class="info-value">{{ (archive && archive.currentLevel) || user.level }}级</text>
      </view>
      <view class="info-row">
        <text class="info-label">累计打卡</text>
        <text class="info-value">{{ (archive && archive.totalCheckin) || 0 }}次</text>
      </view>
      <view class="info-row">
        <text class="info-label">累计考试</text>
        <text class="info-value">{{ (archive && archive.totalExam) || 0 }}次</text>
      </view>
      <view class="info-row">
        <text class="info-label">累计训练时长</text>
        <text class="info-value">{{ (archive && archive.totalTrainHours) || 0 }}小时</text>
      </view>
    </view>

    <view class="card">
      <text class="section-title">等级晋升</text>
      <view class="promote-row">
        <text class="info-label">目标等级</text>
        <picker v-if="targetLevels.length" :range="targetLevels" @change="onTargetChange">
          <view class="promote-picker">{{ targetLevel }}级 ▾</view>
        </picker>
        <text v-else class="promote-picker">已是最高等级</text>
      </view>
      <input class="reason-input" v-model="reason" placeholder="晋升依据（可选）" />
      <button class="apply-btn" :class="{ disabled: !targetLevels.length }" :disabled="!targetLevels.length" :loading="applying" @click="apply">提交晋升申请</button>
    </view>

    <view class="card">
      <text class="section-title">我的台账</text>
      <view class="info-row">
        <text class="info-label">错题数</text>
        <text class="info-value">{{ weak.wrongQuestionCount || 0 }}题</text>
      </view>
      <view class="info-row">
        <text class="info-label">不合格打卡</text>
        <text class="info-value">{{ weak.weakCheckinCount || 0 }}次</text>
      </view>
      <button class="apply-btn" @click="showChangePwd">修改密码</button>
    </view>

    <button class="logout-btn" @click="logout">退出登录</button>

    <view class="mask" v-if="pwdVisible" @click="pwdVisible = false">
      <view class="pwd-box" @click.stop>
        <text class="section-title">修改密码</text>
        <input class="reason-input" v-model="pwdForm.oldPassword" type="password" placeholder="原密码" />
        <input class="reason-input" v-model="pwdForm.newPassword" type="password" placeholder="新密码(至少6位)" />
        <button class="apply-btn" :loading="changing" @click="doChangePwd">确认修改</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyArchive, applyPromotion, getWeakPoints, changePassword } from '@/api'

const user = ref(uni.getStorageSync('user') || {})
const archive = ref(null)
const targetLevel = ref(2)
const reason = ref('')
const targetLevels = computed(() => {
  const current = user.value.level || 1
  const list = []
  for (let i = current + 1; i <= 5; i++) {
    list.push(`${i}级`)
  }
  return list
})
const applying = ref(false)
const weak = ref({})
const pwdVisible = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const changing = ref(false)

const levelText = computed(() => {
  const u = user.value
  if (!u) return ''
  if (u.roles && u.roles.includes('SUPER_ADMIN')) return '超级管理员'
  if (u.roles && u.roles.includes('DEPT_LEADER')) return '部门负责人'
  return `${u.level}级救生员`
})

const load = async () => {
  const res = await getMyArchive()
  archive.value = res.data
  const w = await getWeakPoints()
  weak.value = w.data
  const current = user.value.level || 1
  if (targetLevel.value <= current) {
    targetLevel.value = Math.min(current + 1, 5)
  }
}

const showChangePwd = () => {
  pwdForm.value = { oldPassword: '', newPassword: '' }
  pwdVisible.value = true
}

const doChangePwd = async () => {
  changing.value = true
  try {
    await changePassword(pwdForm.value)
    uni.showToast({ title: '密码修改成功', icon: 'success' })
    pwdVisible.value = false
  } catch (e) {
    uni.showToast({ title: e.message || '修改失败', icon: 'none' })
  } finally {
    changing.value = false
  }
}

const onTargetChange = (e) => {
  const current = user.value.level || 1
  targetLevel.value = current + 1 + Number(e.detail.value)
}

const apply = async () => {
  if (!targetLevels.value.length) {
    uni.showToast({ title: '当前已是最高等级', icon: 'none' })
    return
  }
  applying.value = true
  try {
    await applyPromotion({ toLevel: targetLevel.value, reason: reason.value })
    uni.showToast({ title: '申请已提交', icon: 'success' })
    reason.value = ''
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  } finally {
    applying.value = false
  }
}

const logout = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
  uni.reLaunch({ url: '/pages/login/login' })
}

onShow(load)
</script>

<style scoped>
.profile-card { display: flex; flex-direction: column; align-items: center; padding: 40rpx 24rpx; }
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #005A9C;
  color: #fff;
  font-size: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.profile-name { font-size: 34rpx; font-weight: 600; color: #333; margin-top: 16rpx; }
.profile-level { font-size: 26rpx; color: #005A9C; margin-top: 8rpx; }
.info-row { display: flex; justify-content: space-between; padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.info-label { font-size: 28rpx; color: #666; }
.info-value { font-size: 28rpx; color: #333; font-weight: 600; }
.promote-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; }
.promote-picker { font-size: 28rpx; color: #005A9C; font-weight: 600; }
.reason-input { background: #F5F7FA; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; margin-top: 12rpx; }
.apply-btn { background: #005A9C; color: #fff; border-radius: 12rpx; margin-top: 24rpx; }
.apply-btn.disabled { background: #B8C2CC; }
.apply-btn::after { border: none; }
.logout-btn { background: #fff; color: #F53F3F; border-radius: 12rpx; margin: 40rpx 20rpx; }
.logout-btn::after { border: none; }
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.pwd-box { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 40rpx; }
</style>

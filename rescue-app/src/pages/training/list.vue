<template>
  <view>
    <scroll-view scroll-x class="level-tabs">
      <view
        v-for="i in 5"
        :key="i"
        class="level-tab"
        :class="{ active: level === i }"
        @click="switchLevel(i)"
      >{{ i }}级</view>
    </scroll-view>

    <view class="card proj-card" v-for="p in list" :key="p.id" @click="goDetail(p)">
      <view class="proj-head">
        <text class="proj-name">{{ p.name }}</text>
        <text class="proj-level">{{ p.level }}级</text>
      </view>
      <text class="proj-category">{{ p.category || '未分类' }}</text>
      <text class="proj-outline" v-if="p.outline">{{ p.outline }}</text>
    </view>

    <view v-if="!loading && list.length === 0" class="empty">暂无训练项目，请等待后台导入</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getProjectPage } from '@/api'

const list = ref([])
const loading = ref(false)
const level = ref(1)

const load = async () => {
  loading.value = true
  try {
    const res = await getProjectPage({ page: 1, size: 100, level: level.value })
    list.value = res.data.records
  } finally {
    loading.value = false
  }
}

const switchLevel = (i) => {
  level.value = i
  load()
}

const goDetail = (p) => {
  uni.navigateTo({ url: `/pages/training/detail?id=${p.id}` })
}

onShow(() => {
  const user = uni.getStorageSync('user')
  if (user && user.level) level.value = user.level
  load()
})
</script>

<style scoped>
.level-tabs {
  white-space: nowrap;
  background: #fff;
  padding: 20rpx;
  display: flex;
}
.level-tab {
  display: inline-block;
  padding: 12rpx 40rpx;
  margin-right: 16rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  color: #666;
  background: #F5F7FA;
}
.level-tab.active { background: #005A9C; color: #fff; }
.proj-card { margin-top: 20rpx; }
.proj-head { display: flex; justify-content: space-between; align-items: center; }
.proj-name { font-size: 32rpx; font-weight: 600; color: #333; }
.proj-level { font-size: 24rpx; color: #005A9C; background: #cce2ef; padding: 4rpx 16rpx; border-radius: 20rpx; }
.proj-category { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.proj-outline { font-size: 26rpx; color: #666; margin-top: 12rpx; display: block; }
.empty { text-align: center; color: #999; padding: 100rpx 0; font-size: 28rpx; }
</style>

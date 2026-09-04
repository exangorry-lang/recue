<template>
  <div class="card">
    <div class="page-title">系统日志</div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="操作日志" name="op">
        <el-table :data="opList" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="操作人" width="110" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="action" label="操作内容" min-width="180" />
          <el-table-column prop="opTime" label="时间" width="170" />
          <el-table-column label="结果" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="登录日志" name="login">
        <el-table :data="loginList" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="账号" width="130" />
          <el-table-column prop="loginIp" label="登录IP" width="140" />
          <el-table-column label="终端" width="100">
            <template #default="{ row }">{{ row.deviceType === 1 ? '安卓APP' : 'PC后台' }}</template>
          </el-table-column>
          <el-table-column prop="loginTime" label="登录时间" width="170" />
          <el-table-column label="结果" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getOpLogPage, getLoginLogPage } from '@/api'

const activeTab = ref('op')
const opList = ref([])
const loginList = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const [op, login] = await Promise.all([getOpLogPage({ page: 1, size: 50 }), getLoginLogPage({ page: 1, size: 50 })])
    opList.value = op.data.records
    loginList.value = login.data.records
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(activeTab, load)
</script>

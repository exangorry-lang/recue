<template>
  <div class="card">
    <div class="page-title">档案与晋升审批</div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审批晋升" name="pending">
        <el-table :data="pendingList" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="队员ID" width="90" />
          <el-table-column label="晋升" width="110">
            <template #default="{ row }">{{ row.fromLevel }}级 → {{ row.toLevel }}级</template>
          </el-table-column>
          <el-table-column prop="reason" label="晋升依据" min-width="200" show-overflow-tooltip />
          <el-table-column prop="applyTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="approve(row, 1)">通过</el-button>
              <el-button link type="danger" @click="approve(row, 2)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的晋升记录" name="my">
        <el-table :data="myList" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="晋升" width="110">
            <template #default="{ row }">{{ row.fromLevel }}级 → {{ row.toLevel }}级</template>
          </el-table-column>
          <el-table-column prop="reason" label="晋升依据" min-width="200" show-overflow-tooltip />
          <el-table-column prop="applyTime" label="申请时间" width="170" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
                {{ ['待终审', '已通过', '已驳回'][row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingPromotions, getPromotionList, approvePromotion } from '@/api'

const activeTab = ref('pending')
const pendingList = ref([])
const myList = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const [p, m] = await Promise.all([getPendingPromotions(), getPromotionList()])
    pendingList.value = p.data
    myList.value = m.data
  } finally {
    loading.value = false
  }
}

const approve = (row, result) => {
  const text = result === 1 ? '通过' : '驳回'
  ElMessageBox.confirm(`确认${text}该晋升申请（${row.fromLevel}级 → ${row.toLevel}级）？`, '提示', { type: 'warning' })
    .then(async () => {
      await approvePromotion(row.id, { result })
      ElMessage.success('审批完成')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

<template>
  <div class="card">
    <div class="page-title">打卡审核</div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="队员ID" width="90" />
      <el-table-column prop="projectId" label="训练项目ID" width="110" />
      <el-table-column prop="checkinTime" label="打卡时间" width="170" />
      <el-table-column label="自评" width="100">
        <template #default="{ row }">{{ ['', '合格', '基本合格', '不合格'][row.selfEval] || '-' }}</template>
      </el-table-column>
      <el-table-column label="进度" width="90">
        <template #default="{ row }">{{ row.progress }}%</template>
      </el-table-column>
      <el-table-column prop="selfComment" label="自评说明" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" @click="review(row, 1)">通过</el-button>
          <el-button link type="danger" @click="review(row, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, sizes, prev, pager, next"
      :total="total"
      :page-sizes="[10, 20, 50]"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      @current-change="load"
      @size-change="load"
    />

    <el-dialog v-model="dialog.visible" :title="dialog.result === 1 ? '通过打卡' : '驳回打卡'" width="420px">
      <el-input v-model="dialog.comment" type="textarea" :rows="3" placeholder="审核意见（可选）" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button :type="dialog.result === 1 ? 'success' : 'danger'" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewPage, reviewCheckin } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })
const dialog = reactive({ visible: false, result: 1, comment: '', checkinId: null })

const load = async () => {
  loading.value = true
  try {
    const res = await getReviewPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const review = (row, result) => {
  dialog.checkinId = row.id
  dialog.result = result
  dialog.comment = ''
  dialog.visible = true
}

const save = async () => {
  await reviewCheckin(dialog.checkinId, { result: dialog.result, comment: dialog.comment })
  ElMessage.success('审核完成')
  dialog.visible = false
  load()
}

onMounted(load)
</script>

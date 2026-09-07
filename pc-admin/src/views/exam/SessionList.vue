<template>
  <div class="card">
    <div class="page-title">考试管理</div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="考试场次" name="session">
        <div class="toolbar">
          <el-select v-model="query.level" placeholder="等级" clearable style="width: 120px">
            <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
          </el-select>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button v-if="userStore.isSuperAdmin" type="success" @click="openAdd">创建场次</el-button>
        </div>
        <el-table :data="list" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="考试名称" min-width="180" />
          <el-table-column label="等级" width="80">
            <template #default="{ row }">{{ row.level }}级</template>
          </el-table-column>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ row.examType === 1 ? '模拟自测' : '正式晋升' }}</template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分)" width="90" />
          <el-table-column prop="passScore" label="及格线" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">{{ ['未开始', '进行中', '已结束'][row.status] || row.status }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="待人工阅卷" name="review">
        <el-table :data="reviewList" border stripe v-loading="loading">
          <el-table-column prop="id" label="记录ID" width="90" />
          <el-table-column prop="sessionId" label="场次ID" width="90" />
          <el-table-column label="考生" width="100">
            <template #default="{ row }">{{ userName(row.userId) }}</template>
          </el-table-column>
          <el-table-column prop="submitTime" label="交卷时间" width="170" />
          <el-table-column label="当前得分" width="110">
            <template #default="{ row }">{{ row.score ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openReview(row)">阅卷</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialog.visible" title="创建考试场次" width="520px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="考试名称" required><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="dialog.form.level" style="width: 100%">
            <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="dialog.form.examType">
            <el-radio :label="1">模拟自测</el-radio>
            <el-radio :label="2">正式晋升考试</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="dialog.form.duration" :min="1" /></el-form-item>
        <el-form-item label="卷面总分"><el-input-number v-model="dialog.form.totalScore" :min="1" /></el-form-item>
        <el-form-item label="及格线"><el-input-number v-model="dialog.form.passScore" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">创建并随机组卷</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialog.visible" title="人工阅卷" width="420px">
      <el-form label-width="90px">
        <el-form-item label="最终得分">
          <el-input-number v-model="reviewDialog.score" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveReview">确认得分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionList, createSession, getReviewPending, reviewExam, getUserAll } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const activeTab = ref('session')
const list = ref([])
const reviewList = ref([])
const loading = ref(false)
const query = reactive({ level: null })
const dialog = reactive({ visible: false, form: {} })
const reviewDialog = reactive({ visible: false, score: 60, recordId: null })
const userMap = ref({})
const userName = (id) => userMap.value[id] || `#${id}`

const load = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.level) params.level = query.level
    const res = await getSessionList(params)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const loadReview = async () => {
  const [res, u] = await Promise.all([getReviewPending(), getUserAll()])
  reviewList.value = res.data
  const um = {}
  u.data.forEach(x => { um[x.id] = x.realName })
  userMap.value = um
}

const openAdd = () => {
  dialog.form = { name: '', level: 1, examType: 1, duration: 60, totalScore: 100, passScore: 60 }
  dialog.visible = true
}

const save = async () => {
  if (!dialog.form.name) {
    ElMessage.warning('请填写考试名称')
    return
  }
  await createSession(dialog.form)
  ElMessage.success('创建成功，已随机组卷')
  dialog.visible = false
  load()
}

const openReview = (row) => {
  reviewDialog.recordId = row.id
  reviewDialog.score = row.score ?? 60
  reviewDialog.visible = true
}

const saveReview = async () => {
  await reviewExam(reviewDialog.recordId, { score: reviewDialog.score })
  ElMessage.success('阅卷完成')
  reviewDialog.visible = false
  loadReview()
}

onMounted(() => {
  load()
  loadReview()
})
</script>

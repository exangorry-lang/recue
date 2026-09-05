<template>
  <div class="card">
    <div class="page-title">团队协同训练管理</div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="训练团队" name="group">
        <div class="toolbar">
          <el-button type="success" @click="openAddGroup">新建团队</el-button>
        </div>
        <el-table :data="groups" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="团队名称" min-width="200" />
          <el-table-column label="负责人" width="110">
            <template #default="{ row }">{{ userName(row.leaderId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="训练任务" name="task">
        <div class="toolbar">
          <el-button type="success" @click="openAddTask">下发任务</el-button>
        </div>
        <el-table :data="tasks" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="任务/演练名称" min-width="180" />
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ row.taskType === 1 ? '日常实训' : '团队救援演练' }}</template>
          </el-table-column>
          <el-table-column label="团队" width="150">
            <template #default="{ row }">{{ groupName(row.groupId) }}</template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="170" />
          <el-table-column prop="endTime" label="结束时间" width="170" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">{{ ['未开始', '进行中', '已结束'][row.status] || row.status }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openScore(row)">参与打分</el-button>
              <el-button link type="warning" @click="openReview(row)">复盘</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="groupDialog.visible" title="新建团队" width="420px">
      <el-form :model="groupDialog.form" label-width="90px">
        <el-form-item label="团队名称" required>
          <el-input v-model="groupDialog.form.name" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="groupDialog.form.leaderId" style="width: 100%" clearable placeholder="选择负责人">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="groupDialog.form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialog.visible" title="下发任务" width="520px">
      <el-form :model="taskDialog.form" label-width="90px">
        <el-form-item label="任务名称" required>
          <el-input v-model="taskDialog.form.name" />
        </el-form-item>
        <el-form-item label="团队ID" required>
          <el-input v-model="taskDialog.form.groupId" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="taskDialog.form.taskType">
            <el-radio :label="1">日常实训</el-radio>
            <el-radio :label="2">团队救援演练</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务说明">
          <el-input v-model="taskDialog.form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="taskDialog.form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="taskDialog.form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveTask">下发</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scoreDialog.visible" :title="`参与打分 - ${scoreDialog.taskName}`" width="640px">
      <el-table :data="scoreDialog.records" border stripe>
        <el-table-column prop="userName" label="队员" width="120" />
        <el-table-column prop="checkinTime" label="打卡时间" width="170" />
        <el-table-column label="贡献评分" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.contribution" :min="0" :max="100" :precision="1" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="评语" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.comment" size="small" placeholder="评语(可选)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveScore(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!scoreDialog.records.length" style="text-align:center;color:#999;padding:20px">暂无队员参与记录</div>
    </el-dialog>

    <el-dialog v-model="reviewDialog.visible" :title="`演练复盘 - ${reviewDialog.taskName}`" width="640px">
      <el-form label-width="90px">
        <el-form-item label="复盘总结"><el-input v-model="reviewDialog.form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="问题台账"><el-input v-model="reviewDialog.form.problems" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="改进措施"><el-input v-model="reviewDialog.form.improvements" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div v-if="reviewDialog.list.length">
        <div style="font-weight:600;margin:12px 0 8px">历史复盘</div>
        <div v-for="(r, i) in reviewDialog.list" :key="i" style="background:#F5F7FA;padding:10px;border-radius:6px;margin-bottom:8px;font-size:13px">
          <div><b>总结：</b>{{ r.summary || '-' }}</div>
          <div><b>问题：</b>{{ r.problems || '-' }}</div>
          <div><b>改进：</b>{{ r.improvements || '-' }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="saveReview">保存复盘</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeamList, createTeam, getTaskList, createTask, getUserAll, getTaskRecords, scoreTeamRecord, getTeamReview, addTeamReview } from '@/api'

const activeTab = ref('group')
const groups = ref([])
const tasks = ref([])
const loading = ref(false)

const groupDialog = reactive({ visible: false, form: {} })
const taskDialog = reactive({ visible: false, form: {} })
const users = ref([])
const userMap = ref({})
const groupMap = ref({})
const userName = (id) => userMap.value[id] || `#${id}`
const groupName = (id) => groupMap.value[id] || `#${id}`

const load = async () => {
  loading.value = true
  try {
    const [g, t, u] = await Promise.all([getTeamList(), getTaskList(), getUserAll()])
    groups.value = g.data
    tasks.value = t.data
    users.value = u.data
    const gm = {}
    g.data.forEach(x => { gm[x.id] = x.name })
    groupMap.value = gm
    const um = {}
    u.data.forEach(x => { um[x.id] = x.realName })
    userMap.value = um
  } finally {
    loading.value = false
  }
}

const openAddGroup = () => {
  groupDialog.form = { name: '', leaderId: null, remark: '', status: 1 }
  groupDialog.visible = true
}

const saveGroup = async () => {
  if (!groupDialog.form.name) {
    ElMessage.warning('请填写团队名称')
    return
  }
  await createTeam(groupDialog.form)
  ElMessage.success('创建成功')
  groupDialog.visible = false
  load()
}

const openAddTask = () => {
  taskDialog.form = { name: '', groupId: null, taskType: 1, content: '', startTime: null, endTime: null }
  taskDialog.visible = true
}

const saveTask = async () => {
  if (!taskDialog.form.name || !taskDialog.form.groupId) {
    ElMessage.warning('请填写任务名称和团队ID')
    return
  }
  await createTask(taskDialog.form)
  ElMessage.success('任务已下发')
  taskDialog.visible = false
  load()
}

const scoreDialog = reactive({ visible: false, taskName: '', records: [] })
const reviewDialog = reactive({ visible: false, taskName: '', taskId: null, form: {}, list: [] })

const openScore = async (task) => {
  const res = await getTaskRecords(task.id)
  scoreDialog.taskName = task.name
  scoreDialog.records = res.data || []
  scoreDialog.visible = true
}

const saveScore = async (row) => {
  await scoreTeamRecord(row.id, { contribution: row.contribution, comment: row.comment })
  ElMessage.success('评分已保存')
}

const openReview = async (task) => {
  const res = await getTeamReview(task.id)
  reviewDialog.taskId = task.id
  reviewDialog.taskName = task.name
  reviewDialog.form = { summary: '', problems: '', improvements: '' }
  reviewDialog.list = res.data || []
  reviewDialog.visible = true
}

const saveReview = async () => {
  await addTeamReview({ taskId: reviewDialog.taskId, ...reviewDialog.form })
  ElMessage.success('复盘已保存')
  reviewDialog.visible = false
}

onMounted(load)
</script>

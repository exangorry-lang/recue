<template>
  <div class="card">
    <div class="page-title">题库管理</div>
    <div class="toolbar">
      <el-select v-model="query.level" placeholder="等级" clearable style="width: 120px">
        <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
      </el-select>
      <el-select v-model="query.questionType" placeholder="题型" clearable style="width: 120px">
        <el-option label="单选" :value="1" />
        <el-option label="多选" :value="2" />
        <el-option label="判断" :value="3" />
        <el-option label="简答" :value="4" />
      </el-select>
      <el-input v-model="query.chapter" placeholder="章节" clearable style="width: 160px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="warning" @click="openImport">批量导入</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="等级" width="80">
        <template #default="{ row }">{{ row.level }}级</template>
      </el-table-column>
      <el-table-column label="题型" width="80">
        <template #default="{ row }">{{ typeText(row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="chapter" label="章节" width="120" />
      <el-table-column prop="content" label="题干" min-width="260" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" width="120" show-overflow-tooltip />
      <el-table-column prop="score" label="分值" width="80" />
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

    <el-dialog v-model="importVisible" title="批量导入题库（JSON）" width="640px">
      <el-alert
        title='粘贴 JSON 数组，字段：level、chapter、questionType(1单选/2多选/3判断/4简答)、content、options、answer、analysis、score'
        type="info" :closable="false" style="margin-bottom: 12px"
      />
      <el-input v-model="importText" type="textarea" :rows="12"
        placeholder='[{"level":1,"chapter":"基础理论","questionType":1,"content":"题干...","options":"[\"A.xx\",\"B.xx\"]","answer":"A","analysis":"解析..."}]' />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getQuestionPage, importQuestions } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, level: null, questionType: null, chapter: '' })
const importVisible = ref(false)
const importText = ref('')

const typeText = (t) => ({ 1: '单选', 2: '多选', 3: '判断', 4: '简答' }[t] || t)

const load = async () => {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.level) params.level = query.level
    if (query.questionType) params.questionType = query.questionType
    if (query.chapter) params.chapter = query.chapter
    const res = await getQuestionPage(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openImport = () => {
  importText.value = ''
  importVisible.value = true
}

const doImport = async () => {
  try {
    const data = JSON.parse(importText.value)
    if (!Array.isArray(data)) {
      ElMessage.warning('请输入 JSON 数组')
      return
    }
    const res = await importQuestions(data)
    ElMessage.success(res.msg || '导入成功')
    importVisible.value = false
    load()
  } catch (e) {
    ElMessage.error('JSON 格式错误')
  }
}

onMounted(load)
</script>

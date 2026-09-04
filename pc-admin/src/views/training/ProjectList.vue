<template>
  <div class="card">
    <div class="page-title">训练项目管理</div>
    <div class="toolbar">
      <el-select v-model="query.level" placeholder="等级" clearable style="width: 120px">
        <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="项目名称" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="warning" @click="openImport">JSON导入</el-button>
      <el-button type="success" @click="excelInput.click()">Excel导入</el-button>
      <input ref="excelInput" type="file" accept=".xlsx,.xls" style="display: none" @change="onExcelChange" />
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="等级" width="80">
        <template #default="{ row }">{{ row.level }}级</template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="name" label="项目名称" min-width="200" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
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

    <el-dialog v-model="importVisible" title="批量导入训练项目（JSON）" width="640px">
      <el-alert
        title='粘贴 JSON 数组，字段：level(1-5)、category、name、outline、steps、standard、tips、sort'
        type="info" :closable="false" style="margin-bottom: 12px"
      />
      <el-input v-model="importText" type="textarea" :rows="12"
        placeholder='[{"level":1,"category":"基础","name":"水下装备穿戴","outline":"...","steps":"...","standard":"...","tips":"..."}]' />
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
import { getProjectPage, importProjects, importProjectsExcel } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, level: null, keyword: '' })
const importVisible = ref(false)
const importText = ref('')
const excelInput = ref()

const load = async () => {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.level) params.level = query.level
    if (query.keyword) params.keyword = query.keyword
    const res = await getProjectPage(params)
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
    const res = await importProjects(data)
    ElMessage.success(res.msg || '导入成功')
    importVisible.value = false
    load()
  } catch (e) {
    ElMessage.error('JSON 格式错误')
  }
}

const onExcelChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await importProjectsExcel(formData)
    ElMessage.success(res.msg || '导入成功')
    load()
  } catch (err) {
    ElMessage.error('Excel 导入失败')
  } finally {
    e.target.value = ''
  }
}

onMounted(load)
</script>

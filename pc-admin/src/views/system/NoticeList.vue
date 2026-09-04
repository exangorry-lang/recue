<template>
  <div class="card">
    <div class="page-title">通知公告管理</div>
    <div class="toolbar">
      <el-button type="success" @click="openAdd">发布通知/公告</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.noticeType === 1 ? 'primary' : 'warning'">{{ row.noticeType === 1 ? '通知' : '公告' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size"
      @current-change="load" @size-change="load"
    />

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑' : '发布通知/公告'" width="560px">
      <el-form :model="dialog.form" label-width="80px">
        <el-form-item label="标题" required><el-input v-model="dialog.form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="dialog.form.noticeType">
            <el-radio :label="1">通知</el-radio>
            <el-radio :label="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="dialog.form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticePage, createNotice, updateNotice, deleteNotice } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })
const dialog = reactive({ visible: false, isEdit: false, form: {} })

const load = async () => {
  loading.value = true
  try {
    const res = await getNoticePage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  dialog.isEdit = false
  dialog.form = { title: '', content: '', noticeType: 1, status: 1 }
  dialog.visible = true
}

const openEdit = (row) => {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}

const save = async () => {
  if (!dialog.form.title) {
    ElMessage.warning('请填写标题')
    return
  }
  if (dialog.isEdit) {
    await updateNotice(dialog.form)
  } else {
    await createNotice(dialog.form)
  }
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}

const remove = (row) => {
  ElMessageBox.confirm('确认删除该通知？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteNotice(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

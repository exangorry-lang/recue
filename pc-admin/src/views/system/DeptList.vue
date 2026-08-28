<template>
  <div class="card">
    <div class="page-title">组织架构</div>
    <div class="toolbar">
      <el-button type="success" @click="openAdd">新增部门/班组</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" row-key="id" default-expand-all>
      <el-table-column prop="deptName" label="名称" min-width="200" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.deptType === 1 ? 'primary' : 'warning'">{{ row.deptType === 1 ? '部门' : '班组/分队' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑组织' : '新增组织'" width="460px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="dialog.form.deptName" />
        </el-form-item>
        <el-form-item label="上级组织">
          <el-select v-model="dialog.form.parentId" style="width: 100%" clearable>
            <el-option :label="'顶级（无上级）'" :value="0" />
            <el-option v-for="d in list" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="dialog.form.deptType">
            <el-radio :label="1">部门</el-radio>
            <el-radio :label="2">班组/分队</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" />
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
import { getDeptList, createDept, updateDept, deleteDept } from '@/api'

const list = ref([])
const loading = ref(false)
const dialog = reactive({ visible: false, isEdit: false, form: {} })

const load = async () => {
  loading.value = true
  try {
    const res = await getDeptList()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  dialog.isEdit = false
  dialog.form = { deptName: '', parentId: 0, deptType: 1, sort: 0, status: 1, remark: '' }
  dialog.visible = true
}

const openEdit = (row) => {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}

const save = async () => {
  if (!dialog.form.deptName) {
    ElMessage.warning('请填写名称')
    return
  }
  if (dialog.isEdit) {
    await updateDept(dialog.form)
  } else {
    await createDept(dialog.form)
  }
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}

const remove = (row) => {
  ElMessageBox.confirm(`确认删除「${row.deptName}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteDept(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

<template>
  <div class="card">
    <div class="page-title">用户管理</div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="账号/姓名/手机号" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openAdd">新增用户</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="等级" width="80">
        <template #default="{ row }">{{ row.rescueLevel }}级</template>
      </el-table-column>
      <el-table-column label="在岗状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.jobStatus === 1 ? 'success' : 'info'">{{ row.jobStatus === 1 ? '在岗' : '离岗' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
      <el-table-column label="操作" min-width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openLevel(row)">调级</el-button>
          <el-button link :type="row.enabled === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.enabled === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="primary" @click="resetPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="账号" required>
          <el-input v-model="dialog.form.username" :disabled="dialog.isEdit" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="dialog.form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="dialog.form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="dialog.form.deptId" style="width: 100%" clearable placeholder="请选择">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="救生等级">
          <el-select v-model="dialog.form.rescueLevel" style="width: 100%">
            <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="在岗状态">
          <el-radio-group v-model="dialog.form.jobStatus">
            <el-radio :label="1">在岗</el-radio>
            <el-radio :label="2">离岗</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="levelDialog.visible" title="调整等级" width="360px">
      <el-select v-model="levelDialog.level" style="width: 100%">
        <el-option v-for="i in 5" :key="i" :label="i + '级'" :value="i" />
      </el-select>
      <template #footer>
        <el-button @click="levelDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveLevel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, createUser, updateUser, deleteUser, updateUserStatus, updateUserLevel, resetPassword, getDeptList } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const depts = ref([])
const query = reactive({ page: 1, size: 10, keyword: '' })

const dialog = reactive({ visible: false, isEdit: false, form: {} })
const levelDialog = reactive({ visible: false, level: 1, userId: null })

const load = async () => {
  loading.value = true
  try {
    const res = await getUserPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadDepts = async () => {
  const res = await getDeptList()
  depts.value = res.data
}

const openAdd = () => {
  dialog.isEdit = false
  dialog.form = { username: '', realName: '', phone: '', deptId: null, rescueLevel: 1, jobStatus: 1 }
  dialog.visible = true
}

const openEdit = (row) => {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}

const save = async () => {
  if (!dialog.form.username || !dialog.form.realName) {
    ElMessage.warning('请填写账号和姓名')
    return
  }
  if (dialog.isEdit) {
    await updateUser(dialog.form)
  } else {
    await createUser(dialog.form)
  }
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}

const remove = (row) => {
  ElMessageBox.confirm(`确认删除用户「${row.realName}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

const toggleStatus = async (row) => {
  await updateUserStatus(row.id, row.enabled === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  load()
}

const resetPwd = (row) => {
  ElMessageBox.confirm(`确认将「${row.realName}」密码重置为默认密码 admin123？`, '提示', { type: 'warning' })
    .then(async () => {
      await resetPassword(row.id)
      ElMessage.success('已重置为 admin123')
    })
    .catch(() => {})
}

const openLevel = (row) => {
  levelDialog.userId = row.id
  levelDialog.level = row.rescueLevel
  levelDialog.visible = true
}

const saveLevel = async () => {
  await updateUserLevel(levelDialog.userId, levelDialog.level)
  ElMessage.success('等级已调整')
  levelDialog.visible = false
  load()
}

onMounted(() => {
  load()
  loadDepts()
})
</script>

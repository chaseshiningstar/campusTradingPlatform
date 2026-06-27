<template>
  <div class="category-manage-page">
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h3 style="margin: 0;">分类管理</h3>
        <el-button type="primary" @click="handleAdd()">
          <el-icon><Plus /></el-icon> 新增大类
        </el-button>
      </div>

      <el-table :data="categoryList" v-loading="loading" border stripe>
        <el-table-column prop="categoryName" label="分类名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="100" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleToggle(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增大类'"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="图标标识(英文)" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getAllCategories,
  addCategory,
  updateCategory,
  deleteCategory,
  toggleCategoryStatus
} from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const categoryList = ref([])

const form = reactive({
  id: null,
  categoryName: '',
  icon: '',
  sortOrder: 0
})

async function loadData() {
  loading.value = true
  try {
    const res = await getAllCategories()
    // 只显示顶层大类
    categoryList.value = (res.data || []).filter(c => c.parentId === 0)
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.categoryName = ''
  form.icon = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.categoryName = row.categoryName
  form.icon = row.icon || ''
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!form.categoryName.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  submitting.value = true
  try {
    const data = {
      id: form.id,
      categoryName: form.categoryName.trim(),
      parentId: 0,
      icon: form.icon || null,
      sortOrder: form.sortOrder || 0
    }

    if (isEdit.value) {
      await updateCategory(data)
      ElMessage.success('更新成功')
    } else {
      await addCategory(data)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

function handleToggle(row) {
  ElMessageBox.confirm(
    `确定${row.status === 1 ? '禁用' : '启用'}「${row.categoryName}」？`,
    '操作确认',
    { type: 'warning' }
  ).then(async () => {
    try {
      await toggleCategoryStatus(row.id)
      ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
      await loadData()
    } catch (error) {
      console.error('切换状态失败:', error)
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定删除「${row.categoryName}」？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
  ).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      await loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-manage-page h3 {
  color: #303133;
}
</style>

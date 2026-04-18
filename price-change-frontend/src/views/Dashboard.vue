<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPriceItems, createPriceItem, deletePriceItem, updatePriceItem,
  getCategories, createCategory, updateCategory, deleteCategory,
  type PriceItem, type Category
} from '../api/priceItem'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()

const items = ref<PriceItem[]>([])
const categories = ref<Category[]>([])
const sortOrder = ref('desc')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<string | null>(null)
const form = ref({
  name: '',
  currentValue: 0,
  targetValue: 0,
  categoryId: null as string | null
})

const activeTab = ref('all')
const activeCategoryId = computed(() => activeTab.value === 'all' ? null : activeTab.value)
const categoryDialogVisible = ref(false)
const categoryForm = ref({ name: '' })
const isCategoryEdit = ref(false)
const editingCategoryId = ref<string | null>(null)
const showCategoryManage = ref(false)

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const categoryIdParam = activeCategoryId.value !== null ? activeCategoryId.value : undefined
    items.value = await getPriceItems(sortOrder.value, categoryIdParam)
  } catch (e: any) {
    const errorMsg = e.response?.data?.message || e.message || '加载失败'
    ElMessage.error(errorMsg)
    items.value = []
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => loadItems()

const handleSort = async (order: string) => {
  sortOrder.value = order
  await loadItems()
}

const handleTableSort = (sortParam: { prop: string; order: string | null }) => {
  if (sortParam.order === 'ascending') {
    handleSort('asc')
  } else if (sortParam.order === 'descending') {
    handleSort('desc')
  } else {
    handleSort('desc')
  }
}

const handleAdd = async () => {
  if (!form.value.name || form.value.name.trim().length === 0) {
    ElMessage.warning('请输入名称')
    return
  }
  if (form.value.name.length > 100) {
    ElMessage.warning('名称长度不能超过100字符')
    return
  }
  if (!form.value.currentValue || form.value.currentValue <= 0) {
    ElMessage.warning('当前值必须大于0')
    return
  }
  if (form.value.currentValue > 999999999) {
    ElMessage.warning('当前值过大，请检查输入')
    return
  }
  if (form.value.targetValue < 0) {
    ElMessage.warning('目标值不能为负数')
    return
  }
  if (form.value.targetValue > 999999999) {
    ElMessage.warning('目标值过大，请检查输入')
    return
  }

  try {
    const requestData = {
      name: form.value.name.trim(),
      currentValue: form.value.currentValue,
      targetValue: form.value.targetValue,
      categoryId: form.value.categoryId
    }

    if (isEdit.value && editingId.value !== null) {
      await updatePriceItem(editingId.value, requestData)
      ElMessage.success('更新成功')
    } else {
      await createPriceItem(requestData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    form.value = { name: '', currentValue: 0, targetValue: 0, categoryId: null }
    isEdit.value = false
    editingId.value = null
    await Promise.all([loadItems(), loadCategories()])
  } catch (e: any) {
    const errorMsg = e.response?.data?.message || e.message || (isEdit.value ? '更新失败' : '添加失败')
    ElMessage.error(errorMsg)
  }
}

const openDialog = (row?: PriceItem) => {
  if (row) {
    isEdit.value = true
    editingId.value = row.id
    form.value = {
      name: row.name,
      currentValue: row.currentValue,
      targetValue: row.targetValue,
      categoryId: row.categoryId
    }
  } else {
    isEdit.value = false
    editingId.value = null
    form.value = { name: '', currentValue: 0, targetValue: 0, categoryId: null }
  }
  dialogVisible.value = true
}

const handleCloseDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.value = { name: '', currentValue: 0, targetValue: 0, categoryId: null }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePriceItem(id)
    ElMessage.success('删除成功')
    await Promise.all([loadItems(), loadCategories()])
  } catch (e: any) {
    if (e !== 'cancel') {
      const errorMsg = e.response?.data?.message || e.message || '删除失败'
      ElMessage.error(errorMsg)
    }
  }
}

const openCategoryDialog = (cat?: Category) => {
  if (cat) {
    isCategoryEdit.value = true
    editingCategoryId.value = cat.id
    categoryForm.value = { name: cat.name }
  } else {
    isCategoryEdit.value = false
    editingCategoryId.value = null
    categoryForm.value = { name: '' }
  }
  categoryDialogVisible.value = true
}

const handleCategorySubmit = async () => {
  if (!categoryForm.value.name || categoryForm.value.name.trim().length === 0) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    if (isCategoryEdit.value && editingCategoryId.value !== null) {
      await updateCategory(editingCategoryId.value, { name: categoryForm.value.name.trim() })
      ElMessage.success('分类更新成功')
    } else {
      await createCategory({ name: categoryForm.value.name.trim() })
      ElMessage.success('分类创建成功')
    }
    categoryDialogVisible.value = false
    categoryForm.value = { name: '' }
    await loadCategories()
  } catch (e: any) {
    const errorMsg = e.response?.data?.message || e.message || '操作失败'
    ElMessage.error(errorMsg)
  }
}

const handleDeleteCategory = async (cat: Category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${cat.name}"吗？关联的记录将被移出该分类。`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteCategory(cat.id)
    ElMessage.success('分类删除成功')
    if (activeTab.value === String(cat.id)) {
      activeTab.value = 'all'
    }
    await Promise.all([loadCategories(), loadItems()])
  } catch (e: any) {
    if (e !== 'cancel') {
      const errorMsg = e.response?.data?.message || e.message || '删除分类失败'
      ElMessage.error(errorMsg)
    }
  }
}

const handleLogout = () => {
  authStore.logout()
  window.location.href = '/login'
}

const openRecoveryWindow = () => {
  window.open('/recovery', '_blank')
}

const openCompoundWindow = () => {
  window.open('/compound', '_blank')
}

const formatPercent = (val: number) => {
  const num = val || 0
  return (num > 0 ? '+' : '') + num.toFixed(2) + '%'
}

const getPercentClass = (val: number) => {
  if (val > 0) return 'percent-up'
  if (val < 0) return 'percent-down'
  return 'percent-zero'
}

const categoryColors = [
  { bg: '#fef2f2', border: '#fecaca', text: '#dc2626' },
  { bg: '#fff7ed', border: '#fed7aa', text: '#ea580c' },
  { bg: '#fffbeb', border: '#fde68a', text: '#d97706' },
  { bg: '#f0fdf4', border: '#bbf7d0', text: '#16a34a' },
  { bg: '#f0fdfa', border: '#99f6e4', text: '#0d9488' },
  { bg: '#eff6ff', border: '#bfdbfe', text: '#2563eb' },
  { bg: '#f5f3ff', border: '#c4b5fd', text: '#7c3aed' },
  { bg: '#fdf2f8', border: '#f9a8d4', text: '#db2777' },
]

const getCategoryColorClass = (categoryId: string | null) => {
  if (!categoryId) return 0
  let hash = 0
  for (let i = 0; i < categoryId.length; i++) {
    hash = categoryId.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash) % categoryColors.length
}

const getCategoryStyle = (categoryId: string | null) => {
  const color = categoryColors[getCategoryColorClass(categoryId)]
  return {
    background: color.bg,
    borderColor: color.border,
    color: color.text
  }
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="header-left">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M19 9L14 14L10 10L7 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 14L19 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>涨跌幅计算器</h1>
      </div>
      <div class="header-right">
        <el-button class="tool-btn compound-btn" type="success" @click="openCompoundWindow">
          <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
            <path d="M12 2V22M17 5H9.5C8.57174 5 7.6815 5.31607 7.02513 5.87868C6.36875 6.44129 6 7.20435 6 8C6 8.79565 6.36875 9.55871 7.02513 10.1213C7.6815 10.6839 8.57174 11 9.5 11H14.5C15.4283 11 16.3185 11.3161 16.9749 11.8787C17.6313 12.4413 18 13.2043 18 14C18 14.7957 17.6313 15.5587 16.9749 16.1213C16.3185 16.6839 15.4283 17 14.5 17H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          复利计算器
        </el-button>
        <el-button class="tool-btn" type="success" @click="openRecoveryWindow">
          <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          回本计算器
        </el-button>
        <div class="user-info">
          <span class="user-avatar">{{ authStore.username?.charAt(0) || 'U' }}</span>
          <span class="user-name">{{ authStore.username }}</span>
        </div>
        <el-button class="logout-btn" type="danger" link @click="handleLogout">
          <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
            <path d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 17L21 12L16 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </el-button>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="toolbar-section">
        <div class="left-section">
          <div class="category-tabs-wrapper">
            <el-tabs v-model="activeTab" class="category-tabs" @tab-change="handleTabChange">
              <el-tab-pane label="全部" name="all" />
              <el-tab-pane
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :name="String(cat.id)"
              />
            </el-tabs>
          </div>
          <div class="category-actions">
            <el-button @click="openCategoryDialog()">+ 新建分类</el-button>
            <el-button type="info" plain @click="showCategoryManage = true">管理分类</el-button>
          </div>
        </div>
        <div class="toolbar">
          <el-button type="primary" class="add-btn" @click="openDialog()">
            <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
              <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            新增记录
          </el-button>
        </div>
      </div>

      <el-table :data="items" v-loading="loading" class="data-table" @sort-change="handleTableSort">
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.categoryName" size="small" effect="light" :style="getCategoryStyle(row.categoryId)">{{ row.categoryName }}</el-tag>
            <span v-else class="no-category">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="currentValue" label="当前值" width="120">
          <template #default="{ row }">
            <span class="num-value">{{ row.currentValue?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetValue" label="目标值" width="120">
          <template #default="{ row }">
            <span class="num-value">{{ row.targetValue?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changePercent" label="涨跌幅" sortable="custom" width="120">
          <template #default="{ row }">
            <span :class="['percent-badge', getPercentClass(row.changePercent)]">
              {{ formatPercent(row.changePercent) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ new Date(row.createdAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑涨跌幅记录' : '新增涨跌幅记录'" width="450px" class="custom-dialog" @close="handleCloseDialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="当前值">
          <el-input-number v-model="form.currentValue" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标值">
          <el-input-number v-model="form.targetValue" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">{{ isEdit ? '更新' : '确定' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="isCategoryEdit ? '编辑分类' : '新增分类'" width="350px" class="custom-dialog">
      <el-form :model="categoryForm" label-position="top">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit">{{ isCategoryEdit ? '更新' : '确定' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCategoryManage" title="管理分类" width="400px" class="custom-dialog">
      <div class="category-list">
        <div v-for="cat in categories" :key="cat.id" class="category-item">
          <span>{{ cat.name }}</span>
          <div class="category-item-actions">
            <el-button size="small" type="primary" link @click="openCategoryDialog(cat)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDeleteCategory(cat)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="showCategoryManage = false">关闭</el-button>
        <el-button type="primary" @click="openCategoryDialog()">新建分类</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
}

.dashboard-header {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 16px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);

  h1 {
    color: #fff;
    font-size: 22px;
    font-weight: 700;
    margin: 0;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  svg {
    width: 24px;
    height: 24px;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;

  &.compound-btn {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255,255,255,0.1);
  border-radius: 20px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  color: rgba(255,255,255,0.9);
  font-size: 14px;
}

.logout-btn {
  color: rgba(255,255,255,0.7);

  &:hover {
    color: #fff;
  }
}

.dashboard-content {
  padding: 24px 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}

.category-tabs-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200px;
}

.category-tabs {
  flex: 1;

  :deep(.el-tabs__item) {
    font-size: 14px;
    font-weight: 500;

    &.is-active {
      color: #e94560;
    }
  }

  :deep(.el-tabs__active-bar) {
    background: #e94560;
  }
}

.category-actions {
  display: flex;
  gap: 8px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.data-table {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);

  :deep(.el-table__header-wrapper th) {
    background: #f8fafc;
    color: #475569;
    font-weight: 600;
  }

  :deep(.el-table__row:hover > td) {
    background: #fef2f2 !important;
  }
}

.num-value {
  font-family: 'Monaco', 'Consolas', monospace;
  color: #1e293b;
}

.time-text {
  color: #64748b;
  font-size: 13px;
}

.percent-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 16px;
  font-weight: 600;
  font-size: 13px;
}

.percent-up {
  background: #fef2f2;
  color: #e94560;
}

.percent-down {
  background: #f0fdf4;
  color: #10b981;
}

.percent-zero {
  background: #f1f5f9;
  color: #64748b;
}

.no-category {
  color: #cbd5e1;
}

.category-list {
  max-height: 400px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  border-bottom: 1px solid #f1f5f9;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f8fafc;
  }

  &:last-child {
    border-bottom: none;
  }
}

.category-item-actions {
  display: flex;
  gap: 8px;
}
</style>
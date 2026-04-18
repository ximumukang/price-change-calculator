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
const editingId = ref<number | null>(null)
const form = ref({
  name: '',
  currentValue: 0,
  targetValue: 0,
  categoryId: null as number | null
})

// 分类相关
const activeTab = ref('all') // 默认选中"全部"
const activeCategoryId = computed(() => activeTab.value === 'all' ? null : Number(activeTab.value))
const categoryDialogVisible = ref(false)
const categoryForm = ref({ name: '' })
const isCategoryEdit = ref(false)
const editingCategoryId = ref<number | null>(null)
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

const handleCategoryChange = () => {
  loadItems()
}

const handleTabChange = () => {
  loadItems()
}

const handleSort = async (order: string) => {
  sortOrder.value = order
  await loadItems()
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

const handleDelete = async (id: number) => {
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

// ===== 分类管理 =====

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
    // 如果当前选中的分类被删除，回到全部
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

const formatPercent = (val: number) => {
  const num = val || 0
  return (num > 0 ? '+' : '') + num.toFixed(2) + '%'
}

const getPercentClass = (val: number) => {
  if (val > 0) return 'percent-up'
  if (val < 0) return 'percent-down'
  return 'percent-zero'
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>涨跌幅计算器</h2>
          <div class="header-right">
            <span>欢迎，{{ authStore.username }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <!-- 分类 Tab 栏 -->
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
          <div class="category-actions">
            <el-button size="small" @click="openCategoryDialog()">+ 新建分类</el-button>
            <el-button size="small" type="info" plain @click="showCategoryManage = true">管理分类</el-button>
          </div>
        </div>

        <div class="toolbar">
          <el-button type="primary" @click="openDialog()">新增</el-button>
          <div class="sort-buttons">
            <el-button :type="sortOrder === 'asc' ? 'success' : 'default'" @click="handleSort('asc')">
              升序
            </el-button>
            <el-button :type="sortOrder === 'desc' ? 'success' : 'default'" @click="handleSort('desc')">
              降序
            </el-button>
          </div>
        </div>

        <el-table :data="items" v-loading="loading" style="width: 100%">
          <el-table-column prop="categoryName" label="分类" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.categoryName" size="small" type="info">{{ row.categoryName }}</el-tag>
              <span v-else class="no-category">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="currentValue" label="当前值">
            <template #default="{ row }">
              {{ row.currentValue?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="targetValue" label="目标值">
            <template #default="{ row }">
              {{ row.targetValue?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="changePercent" label="涨跌幅">
            <template #default="{ row }">
              <span :class="getPercentClass(row.changePercent)">
                {{ formatPercent(row.changePercent) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间">
            <template #default="{ row }">
              {{ new Date(row.createdAt).toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openDialog(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>

    <!-- 价格项新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑涨跌幅记录' : '新增涨跌幅记录'" width="400px" @close="handleCloseDialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select
            v-model="form.categoryId"
            clearable
            placeholder="选择分类"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="当前值">
          <el-input-number v-model="form.currentValue" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="目标值">
          <el-input-number v-model="form.targetValue" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">{{ isEdit ? '更新' : '确定' }}</el-button>
      </template>
    </el-dialog>

    <!-- 分类新增/编辑对话框 -->
    <el-dialog v-model="categoryDialogVisible" :title="isCategoryEdit ? '编辑分类' : '新增分类'" width="350px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit">{{ isCategoryEdit ? '更新' : '确定' }}</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog v-model="showCategoryManage" title="管理分类" width="400px">
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
  background: #f5f5f5;
}
.el-header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.el-main {
  padding: 20px;
}
.category-tabs-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.category-tabs {
  flex: 1;
}
.category-actions {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}
.category-list {
  max-height: 400px;
  overflow-y: auto;
}
.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
}
.category-item:last-child {
  border-bottom: none;
}
.category-item-actions {
  display: flex;
  gap: 8px;
}
.no-category {
  color: #c0c4cc;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.sort-buttons {
  display: flex;
  gap: 8px;
}
.percent-up {
  color: #f56c6c;
  font-weight: bold;
}
.percent-down {
  color: #67c23a;
  font-weight: bold;
}
.percent-zero {
  color: #909399;
}
</style>

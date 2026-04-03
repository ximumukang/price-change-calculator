<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPriceItems, createPriceItem, deletePriceItem, type PriceItem } from '../api/priceItem'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()

const items = ref<PriceItem[]>([])
const sortOrder = ref('desc')
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({
  name: '',
  currentValue: 0,
  targetValue: 0
})

const loadItems = async () => {
  loading.value = true
  try {
    items.value = await getPriceItems(sortOrder.value)
  } catch (e: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSort = async (order: string) => {
  sortOrder.value = order
  await loadItems()
}

const handleAdd = async () => {
  if (!form.value.name || form.value.currentValue <= 0 || form.value.targetValue <= 0) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await createPriceItem({
      name: form.value.name,
      currentValue: form.value.currentValue,
      targetValue: form.value.targetValue
    })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    form.value = { name: '', currentValue: 0, targetValue: 0 }
    await loadItems()
  } catch (e: any) {
    ElMessage.error('添加失败')
  }
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
    await loadItems()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
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
        <div class="toolbar">
          <el-button type="primary" @click="dialogVisible = true">新增</el-button>
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
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDelete(row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>

    <el-dialog v-model="dialogVisible" title="新增涨跌幅记录" width="400px">
      <el-form :model="form" label-width="80px">
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
        <el-button type="primary" @click="handleAdd">确定</el-button>
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

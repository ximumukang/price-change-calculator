<script setup lang="ts">
import { ref, computed } from 'vue'

const declinePercent = ref<number | null>(null)

const requiredGain = computed(() => {
  if (declinePercent.value === null || declinePercent.value <= 0 || declinePercent.value >= 100) {
    return null
  }
  const decline = declinePercent.value
  const gain = (decline / (100 - decline)) * 100
  return gain.toFixed(2)
})

const gainRatio = computed(() => {
  if (!requiredGain.value || !declinePercent.value) return null
  return (parseFloat(requiredGain.value) / declinePercent.value).toFixed(2)
})

const severityLevel = computed(() => {
  if (!declinePercent.value) return null
  const decline = declinePercent.value
  if (decline < 10) return { level: '轻微', color: 'green', icon: '📊' }
  if (decline < 20) return { level: '中度', color: 'yellow', icon: '⚠️' }
  if (decline < 30) return { level: '严重', color: 'orange', icon: '🔴' }
  return { level: '极严重', color: 'red', icon: '💥' }
})

const tableData = computed(() => {
  const data = []
  for (let i = 5; i <= 95; i += 5) {
    const gain = (i / (100 - i)) * 100
    data.push({ decline: i, gain: gain.toFixed(2) })
  }
  return data
})

const getRowSeverity = (decline: number) => {
  if (decline < 10) return 'low'
  if (decline < 20) return 'medium'
  if (decline < 30) return 'high'
  return 'critical'
}

const getRowClass = ({ row }: { row: { decline: number } }) => {
  const classes: string[] = []
  if (declinePercent.value === row.decline) classes.push('highlight-row')
  classes.push(`severity-${getRowSeverity(row.decline)}`)
  return classes.join(' ')
}
</script>

<template>
  <div class="recovery-page">
    <!-- Background decorations -->
    <div class="page-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- Header -->
    <header class="page-header">
      <div class="header-left">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>回本计算器</h1>
      </div>
      <a href="/" class="back-link">
        <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </a>
    </header>

    <main class="page-content">
      <!-- Calculator Card -->
      <div class="calc-card">
        <!-- Input Section -->
        <div class="input-section">
          <div class="section-header">
            <div class="section-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" width="20" height="20">
                <path d="M22 7L13.5 15.5L8.5 10.5L2 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 7H22V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <label class="section-label">输入下跌幅度</label>
          </div>
          
          <div class="input-group">
            <div class="input-with-suffix">
              <el-input-number 
                v-model="declinePercent" 
                :min="0" 
                :max="99.99" 
                :step="0.1" 
                :precision="2" 
                class="decline-input"
                controls-position="right"
                placeholder="输入下跌百分比"
              />
            </div>
            
            <!-- Quick Select -->
            <div class="quick-select">
              <button 
                v-for="val in [5, 10, 20, 30, 50]" 
                :key="val"
                @click="declinePercent = val"
                class="quick-btn"
                :class="{ active: declinePercent === val }"
              >
                {{ val }}%
              </button>
            </div>
          </div>
        </div>

        <!-- Divider -->
        <div class="section-divider"></div>

        <!-- Result Display -->
        <div class="result-section" v-if="requiredGain !== null">
          <div class="severity-badge" :class="severityLevel?.color">
            <span class="severity-icon">{{ severityLevel?.icon }}</span>
            <span class="severity-text">{{ severityLevel?.level }}</span>
          </div>
          
          <div class="result-hero">
            <span class="result-label">需要上涨</span>
            <div class="result-value-row">
              <span class="result-value">{{ requiredGain }}</span>
              <span class="result-unit">%</span>
            </div>
          </div>
          
          <div class="result-stats">
            <div class="stat-card stat-decline">
              <span class="stat-label">下跌幅度</span>
              <span class="stat-value decline-val">{{ declinePercent }}%</span>
            </div>
            <div class="stat-arrow">
              <svg viewBox="0 0 24 24" fill="none" width="20" height="20">
                <path d="M5 12H19M19 12L13 6M19 12L13 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="stat-card stat-gain">
              <span class="stat-label">所需涨幅</span>
              <span class="stat-value gain-val">{{ requiredGain }}%</span>
            </div>
            <div class="stat-card stat-ratio">
              <span class="stat-label">倍数关系</span>
              <span class="stat-value ratio-val">{{ gainRatio }}x</span>
            </div>
          </div>
          
          <div class="insight-box">
            <svg viewBox="0 0 24 24" fill="none" width="16" height="16" class="insight-icon">
              <path d="M9 18H15M12 2L8 10H16L12 22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>下跌 <strong>{{ declinePercent }}%</strong> 后，需要上涨 <strong>{{ requiredGain }}%</strong> 才能回本</span>
          </div>
        </div>

        <!-- Empty State -->
        <div class="empty-state" v-else>
          <div class="empty-visual">
            <svg viewBox="0 0 120 120" fill="none" width="80" height="80">
              <circle cx="60" cy="60" r="50" stroke="currentColor" stroke-width="2" stroke-dasharray="8 4" opacity="0.3"/>
              <path d="M40 70L55 55L65 65L80 45" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
            </svg>
          </div>
          <p class="empty-text">输入下跌幅度查看计算结果</p>
        </div>
      </div>

      <!-- Reference Table Card -->
      <div class="table-card">
        <div class="table-header">
          <div class="table-title-group">
            <div class="table-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M3 9H21M3 15H21M9 3V21M15 3V21" stroke="currentColor" stroke-width="1.5" opacity="0.5"/>
              </svg>
            </div>
            <h3>快速参考表</h3>
          </div>
          <span class="table-badge">{{ tableData.length }} 组数据</span>
        </div>
        
        <el-table :data="tableData" class="ref-table" :row-class-name="getRowClass" stripe>
          <el-table-column label="下跌幅度" align="center">
            <template #default="{ row }">
              <span class="cell-val decline-val">{{ row.decline }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="所需涨幅" align="center">
            <template #default="{ row }">
              <span class="cell-val gain-val">{{ row.gain }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="倍数" align="center" width="120">
            <template #default="{ row }">
              <span class="ratio-badge">{{ (parseFloat(row.gain) / row.decline).toFixed(2) }}x</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* ===== Page & Background ===== */
.recovery-page {
  min-height: 100vh;
  background: #f0f2f5;
  position: relative;
  overflow-x: hidden;
}

.page-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  background: #e94560;
  top: -150px;
  right: -100px;
  opacity: 0.08;
}

.bg-shape-2 {
  width: 400px;
  height: 400px;
  background: #e94560;
  bottom: -100px;
  left: -100px;
  opacity: 0.06;
}

.bg-shape-3 {
  width: 300px;
  height: 300px;
  background: #1e293b;
  top: 40%;
  left: 50%;
  opacity: 0.04;
}

/* ===== Header ===== */
.page-header {
  position: relative;
  z-index: 10;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 16px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);

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
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
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

.back-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
  }
}

/* ===== Content Layout ===== */
.page-content {
  position: relative;
  z-index: 1;
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 24px 60px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ===== Calculator Card ===== */
.calc-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* ===== Input Section ===== */
.input-section {
  padding: 28px 32px 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.section-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e94560;
}

.section-label {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.input-with-suffix {
  position: relative;
}

.decline-input {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 8px 12px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    background: #f8fafc;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 0 0 1px #e94560 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px rgba(233, 69, 96, 0.15) inset;
      background: #fff;
    }
  }

  :deep(.el-input__inner) {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
    text-align: center;
  }

  :deep(.el-input-number__decrease),
  :deep(.el-input-number__increase) {
    border-color: #e2e8f0;
    background: #f8fafc;
    color: #64748b;
    width: 36px;
    transition: all 0.2s;

    &:hover {
      color: #e94560;
      background: #fef2f2;
    }
  }
}

.quick-select {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-btn {
  padding: 8px 18px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #fef2f2;
    border-color: #e94560;
    color: #e94560;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(233, 69, 96, 0.15);
  }

  &.active {
    background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
    border-color: transparent;
    color: #fff;
    box-shadow: 0 4px 12px rgba(233, 69, 96, 0.35);
    transform: translateY(-1px);
  }
}

/* ===== Divider ===== */
.section-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
  margin: 0 32px;
}

/* ===== Result Section ===== */
.result-section {
  padding: 28px 32px 32px;
  animation: fadeSlideIn 0.4s ease-out;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.severity-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  margin-bottom: 24px;
  font-weight: 600;
  font-size: 13px;

  &.green {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    color: #16a34a;
  }

  &.yellow {
    background: #fffbeb;
    border: 1px solid #fde68a;
    color: #d97706;
  }

  &.orange {
    background: #fff7ed;
    border: 1px solid #fed7aa;
    color: #ea580c;
  }

  &.red {
    background: #fef2f2;
    border: 1px solid #fecaca;
    color: #dc2626;
  }
}

.severity-icon {
  font-size: 15px;
}

.result-hero {
  text-align: center;
  margin-bottom: 28px;
}

.result-label {
  display: block;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 500;
}

.result-value-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
}

.result-value {
  font-size: 64px;
  font-weight: 800;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.1;
}

.result-unit {
  font-size: 28px;
  font-weight: 700;
  color: #e94560;
  opacity: 0.6;
}

.result-stats {
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;

  &.stat-ratio {
    flex: 0 0 auto;
    min-width: 100px;
    background: linear-gradient(135deg, #fef2f2 0%, #fff5f5 100%);
    border-color: #fecaca;
  }
}

.stat-label {
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 500;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
}

.decline-val {
  color: #ef4444;
}

.gain-val {
  color: #10b981;
}

.ratio-val {
  color: #e94560;
  font-size: 24px;
  font-weight: 800;
}

.stat-arrow {
  display: flex;
  align-items: center;
  color: #cbd5e1;
  flex-shrink: 0;
}

.insight-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: linear-gradient(135deg, #fef2f2 0%, #fff8f8 100%);
  border: 1px solid #fecaca;
  border-radius: 12px;
  padding: 14px 18px;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;

  strong {
    color: #e94560;
    font-weight: 700;
  }
}

.insight-icon {
  flex-shrink: 0;
  color: #e94560;
  margin-top: 2px;
}

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: 48px 20px 56px;
}

.empty-visual {
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-text {
  color: #94a3b8;
  font-size: 15px;
  margin: 0;
  font-weight: 500;
}

/* ===== Table Card ===== */
.table-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid #f1f5f9;

  h3 {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: #1e293b;
  }
}

.table-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

.table-badge {
  padding: 5px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 20px;
  color: #e94560;
  font-size: 12px;
  font-weight: 600;
}

.ref-table {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    background: #f8fafc;
    color: #94a3b8;
    font-weight: 600;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.06em;
  }

  :deep(.el-table__row) {
    &:hover > td {
      background: #f8fafc !important;
    }

    &.highlight-row > td {
      background: #fef2f2 !important;
    }

    &.highlight-row .cell-val {
      font-weight: 700;
    }

    &.highlight-row .ratio-badge {
      background: #fef2f2;
      border-color: #fecaca;
      color: #e94560;
    }
  }
}

.cell-val {
  font-size: 15px;
  font-weight: 600;

  &.decline-val {
    color: #10b981;
  }

  &.gain-val {
    color: #ef4444;
  }
}

.ratio-badge {
  padding: 3px 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .page-header {
    padding: 14px 20px;

    h1 {
      font-size: 18px;
    }
  }

  .page-content {
    padding: 20px 16px 40px;
  }

  .input-section {
    padding: 24px 20px 20px;
  }

  .section-divider {
    margin: 0 20px;
  }

  .result-section {
    padding: 24px 20px 28px;
  }

  .result-value {
    font-size: 48px;
  }

  .result-unit {
    font-size: 22px;
  }

  .result-stats {
    flex-wrap: wrap;
  }

  .stat-card {
    min-width: 80px;
  }

  .stat-arrow {
    display: none;
  }

  .table-header {
    padding: 16px 20px;
  }

  .cell-val {
    font-size: 13px;
  }

  .calc-card,
  .table-card {
    border-radius: 12px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 12px 16px;

    h1 {
      font-size: 16px;
    }
  }

  .logo-icon {
    width: 36px;
    height: 36px;
  }

  .page-content {
    padding: 16px 12px 32px;
  }

  .input-section {
    padding: 20px 16px 16px;
  }

  .section-divider {
    margin: 0 16px;
  }

  .result-section {
    padding: 20px 16px 24px;
  }

  .result-value {
    font-size: 40px;
  }

  .quick-select {
    justify-content: center;
  }

  .result-stats {
    gap: 8px;
  }

  .stat-card {
    padding: 12px 8px;
  }

  .stat-value {
    font-size: 18px;
  }

  .ratio-val {
    font-size: 20px;
  }
}
</style>

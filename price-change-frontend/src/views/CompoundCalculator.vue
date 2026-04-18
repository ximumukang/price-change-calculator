<script setup lang="ts">
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

use([
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
])

const principal = ref<number | null>(10000)
const annualRate = ref<number | null>(5)
const years = ref<number | null>(10)

interface YearData {
  year: number
  startAmount: number
  interest: number
  endAmount: number
  totalInterest: number
}

const yearlyData = computed<YearData[]>(() => {
  if (!principal.value || !annualRate.value || !years.value) return []
  if (principal.value <= 0 || annualRate.value <= 0 || years.value <= 0) return []

  const data: YearData[] = []
  let current = principal.value
  const rate = annualRate.value / 100

  for (let i = 1; i <= years.value; i++) {
    const interest = current * rate
    const endAmount = current + interest
    data.push({
      year: i,
      startAmount: parseFloat(current.toFixed(2)),
      interest: parseFloat(interest.toFixed(2)),
      endAmount: parseFloat(endAmount.toFixed(2)),
      totalInterest: parseFloat((endAmount - principal.value).toFixed(2))
    })
    current = endAmount
  }
  return data
})

const finalAmount = computed(() => {
  const data = yearlyData.value
  if (data.length === 0) return null
  return data[data.length - 1].endAmount
})

const totalInterest = computed(() => {
  const data = yearlyData.value
  if (data.length === 0) return null
  return data[data.length - 1].totalInterest
})

const isValid = computed(() => {
  return principal.value !== null && principal.value > 0 &&
    annualRate.value !== null && annualRate.value > 0 &&
    years.value !== null && years.value > 0
})

const chartOption = computed(() => {
  if (!isValid.value) return {}

  const data = yearlyData.value
  const yearLabels = ['起始', ...data.map(d => `第${d.year}年`)]
  const amountData = [principal.value!, ...data.map(d => d.endAmount)]
  const interestData = [0, ...data.map(d => d.totalInterest)]

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#1e293b', fontSize: 13 },
      formatter: (params: any) => {
        let tip = `<div style="font-weight:600;margin-bottom:6px">${params[0].axisValue}</div>`
        params.forEach((p: any) => {
          tip += `<div style="display:flex;justify-content:space-between;gap:16px;margin:3px 0">
            <span>${p.marker} ${p.seriesName}</span>
            <span style="font-weight:600">${p.value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
          </div>`
        })
        return tip
      }
    },
    legend: {
      data: ['累计金额', '累计利息'],
      bottom: 0,
      textStyle: { color: '#64748b', fontSize: 12 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: yearLabels,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 12,
        formatter: (val: number) => {
          if (val >= 10000) return (val / 10000).toFixed(1) + '万'
          return val.toLocaleString()
        }
      }
    },
    series: [
      {
        name: '累计金额',
        type: 'line',
        data: amountData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#3b82f6' },
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59, 130, 246, 0.25)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
            ]
          }
        }
      },
      {
        name: '累计利息',
        type: 'line',
        data: interestData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#10b981' },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.02)' }
            ]
          }
        }
      }
    ]
  }
})

const formatMoney = (val: number) => {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<template>
  <div class="compound-page">
    <!-- Background -->
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
            <path d="M12 2V22M17 5H9.5C8.57174 5 7.6815 5.31607 7.02513 5.87868C6.36875 6.44129 6 7.20435 6 8C6 8.79565 6.36875 9.55871 7.02513 10.1213C7.6815 10.6839 8.57174 11 9.5 11H14.5C15.4283 11 16.3185 11.3161 16.9749 11.8787C17.6313 12.4413 18 13.2043 18 14C18 14.7957 17.6313 15.5587 16.9749 16.1213C16.3185 16.6839 15.4283 17 14.5 17H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>复利计算器</h1>
      </div>
      <a href="/" class="back-link">
        <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回首页
      </a>
    </header>

    <main class="page-content">
      <!-- Input Card -->
      <div class="input-card">
        <div class="card-title-group">
          <div class="card-icon-wrap blue">
            <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
              <rect x="2" y="3" width="20" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M2 9H22M8 3V9M16 3V9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3>参数设置</h3>
        </div>

        <div class="input-grid">
          <div class="input-item">
            <label class="input-label">初始本金</label>
            <div class="input-with-unit">
              <el-input-number
                v-model="principal"
                :min="1"
                :max="999999999"
                :step="1000"
                :precision="2"
                controls-position="right"
                placeholder="输入本金"
                class="num-input"
              />
              <span class="input-unit">元</span>
            </div>
          </div>

          <div class="input-item">
            <label class="input-label">年利率</label>
            <div class="input-with-unit">
              <el-input-number
                v-model="annualRate"
                :min="0.01"
                :max="100"
                :step="0.1"
                :precision="2"
                controls-position="right"
                placeholder="输入年利率"
                class="num-input"
              />
              <span class="input-unit">%</span>
            </div>
          </div>

          <div class="input-item">
            <label class="input-label">投资年数</label>
            <div class="input-with-unit">
              <el-input-number
                v-model="years"
                :min="1"
                :max="100"
                :step="1"
                :precision="0"
                controls-position="right"
                placeholder="输入年数"
                class="num-input"
              />
              <span class="input-unit">年</span>
            </div>
          </div>
        </div>

        <!-- Quick Presets -->
        <div class="preset-section">
          <span class="preset-label">快速设定：</span>
          <div class="preset-group">
            <button
              v-for="preset in [{ p: 10000, r: 3, y: 5, label: '1万/3%/5年' }, { p: 50000, r: 5, y: 10, label: '5万/5%/10年' }, { p: 100000, r: 8, y: 20, label: '10万/8%/20年' }, { p: 500000, r: 6, y: 30, label: '50万/6%/30年' }]"
              :key="preset.label"
              @click="principal = preset.p; annualRate = preset.r; years = preset.y"
              class="preset-btn"
            >
              {{ preset.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Result Summary -->
      <div v-if="isValid && finalAmount !== null" class="result-card">
        <div class="result-grid">
          <div class="result-item result-final">
            <span class="result-label">最终金额</span>
            <div class="result-value-row">
              <span class="result-value primary">{{ formatMoney(finalAmount) }}</span>
              <span class="result-unit">元</span>
            </div>
          </div>
          <div class="result-divider"></div>
          <div class="result-item result-interest">
            <span class="result-label">总利息</span>
            <div class="result-value-row">
              <span class="result-value green">{{ formatMoney(totalInterest!) }}</span>
              <span class="result-unit">元</span>
            </div>
          </div>
          <div class="result-divider"></div>
          <div class="result-item result-principal">
            <span class="result-label">初始本金</span>
            <div class="result-value-row">
              <span class="result-value dark">{{ formatMoney(principal!) }}</span>
              <span class="result-unit">元</span>
            </div>
          </div>
          <div class="result-divider"></div>
          <div class="result-item result-multiple">
            <span class="result-label">倍数</span>
            <div class="result-value-row">
              <span class="result-value accent">{{ (finalAmount / principal!).toFixed(2) }}</span>
              <span class="result-unit">倍</span>
            </div>
          </div>
        </div>

        <div class="insight-box">
          <svg viewBox="0 0 24 24" fill="none" width="16" height="16" class="insight-icon">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 16V12M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>投入 <strong>{{ formatMoney(principal!) }}</strong> 元，按年利率 <strong>{{ annualRate }}%</strong> 复利计算 <strong>{{ years }}</strong> 年后，最终获得 <strong>{{ formatMoney(finalAmount) }}</strong> 元，其中利息 <strong>{{ formatMoney(totalInterest!) }}</strong> 元</span>
        </div>
      </div>

      <!-- Chart Card -->
      <div v-if="isValid && yearlyData.length > 0" class="chart-card">
        <div class="card-title-group">
          <div class="card-icon-wrap blue">
            <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
              <path d="M22 7L13.5 15.5L8.5 10.5L2 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 7H22V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3>增长趋势</h3>
        </div>
        <v-chart class="chart" :option="chartOption" autoresize />
      </div>

      <!-- Table Card -->
      <div v-if="isValid && yearlyData.length > 0" class="table-card">
        <div class="table-header">
          <div class="card-title-group">
            <div class="card-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M3 9H21M3 15H21M9 3V21M15 3V21" stroke="currentColor" stroke-width="1.5" opacity="0.5"/>
              </svg>
            </div>
            <h3>逐年明细</h3>
          </div>
          <span class="table-badge">{{ yearlyData.length }} 年</span>
        </div>

        <el-table :data="yearlyData" class="detail-table" stripe>
          <el-table-column label="年份" align="center" width="100">
            <template #default="{ row }">
              <span class="year-badge">第{{ row.year }}年</span>
            </template>
          </el-table-column>
          <el-table-column label="期初金额" align="center">
            <template #default="{ row }">
              <span class="cell-val">{{ formatMoney(row.startAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当年利息" align="center">
            <template #default="{ row }">
              <span class="cell-val interest-val">+{{ formatMoney(row.interest) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="期末金额" align="center">
            <template #default="{ row }">
              <span class="cell-val amount-val">{{ formatMoney(row.endAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="累计利息" align="center">
            <template #default="{ row }">
              <span class="cell-val total-interest-val">{{ formatMoney(row.totalInterest) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Empty State -->
      <div v-if="!isValid" class="empty-card">
        <div class="empty-visual">
          <svg viewBox="0 0 120 120" fill="none" width="80" height="80">
            <circle cx="60" cy="60" r="50" stroke="currentColor" stroke-width="2" stroke-dasharray="8 4" opacity="0.3"/>
            <path d="M35 75L50 55L60 65L85 40" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
          </svg>
        </div>
        <p class="empty-text">输入参数查看复利计算结果</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* ===== Page & Background ===== */
.compound-page {
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
  background: #3b82f6;
  top: -150px;
  right: -100px;
  opacity: 0.08;
}

.bg-shape-2 {
  width: 400px;
  height: 400px;
  background: #3b82f6;
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
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
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
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 24px 60px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ===== Card Base ===== */
.input-card,
.result-card,
.chart-card,
.table-card,
.empty-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-title-group {
  display: flex;
  align-items: center;
  gap: 10px;

  h3 {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: #1e293b;
  }
}

.card-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;

  &.blue {
    background: #eff6ff;
    color: #3b82f6;
  }
}

/* ===== Input Card ===== */
.input-card {
  padding: 24px 28px;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.input-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.input-with-unit {
  position: relative;
}

.num-input {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 8px 12px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    background: #f8fafc;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 0 0 1px #3b82f6 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15) inset;
      background: #fff;
    }
  }

  :deep(.el-input__inner) {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
  }

  :deep(.el-input-number__decrease),
  :deep(.el-input-number__increase) {
    border-color: #e2e8f0;
    background: #f8fafc;
    color: #64748b;
    width: 36px;
    transition: all 0.2s;

    &:hover {
      color: #3b82f6;
      background: #eff6ff;
    }
  }
}

.input-unit {
  position: absolute;
  right: 48px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  pointer-events: none;
}

.preset-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #f1f5f9;
  flex-wrap: wrap;
}

.preset-label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.preset-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preset-btn {
  padding: 6px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #eff6ff;
    border-color: #3b82f6;
    color: #3b82f6;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  }
}

/* ===== Result Card ===== */
.result-card {
  padding: 28px;
  animation: fadeSlideIn 0.4s ease-out;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-grid {
  display: flex;
  align-items: stretch;
  gap: 0;
}

.result-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.result-divider {
  width: 1px;
  background: #f1f5f9;
  align-self: stretch;
  margin: 4px 0;
}

.result-label {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 500;
}

.result-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.result-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;

  &.primary {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  &.green {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  &.dark {
    color: #1e293b;
  }

  &.accent {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.result-unit {
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
}

.insight-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f7ff 100%);
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  padding: 14px 18px;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  margin-top: 24px;

  strong {
    color: #3b82f6;
    font-weight: 700;
  }
}

.insight-icon {
  flex-shrink: 0;
  color: #3b82f6;
  margin-top: 2px;
}

/* ===== Chart Card ===== */
.chart-card {
  padding: 24px 28px;
}

.chart {
  width: 100%;
  height: 380px;
}

/* ===== Table Card ===== */
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid #f1f5f9;
}

.table-badge {
  padding: 5px 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 20px;
  color: #3b82f6;
  font-size: 12px;
  font-weight: 600;
}

.detail-table {
  width: 100%;
  max-height: 560px;

  :deep(.el-table__header-wrapper th) {
    background: #f8fafc;
    color: #94a3b8;
    font-weight: 600;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.06em;
  }

  :deep(.el-table__row:hover > td) {
    background: #f8fafc !important;
  }
}

.year-badge {
  padding: 3px 10px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  color: #3b82f6;
  font-size: 12px;
  font-weight: 700;
}

.cell-val {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  font-family: 'Monaco', 'Consolas', monospace;
}

.interest-val {
  color: #10b981;
}

.amount-val {
  color: #3b82f6;
  font-weight: 700;
}

.total-interest-val {
  color: #f59e0b;
}

/* ===== Empty State ===== */
.empty-card {
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

  .input-card {
    padding: 20px;
  }

  .input-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .result-grid {
    flex-wrap: wrap;
    gap: 16px;
  }

  .result-divider {
    display: none;
  }

  .result-item {
    min-width: calc(50% - 8px);
    flex: unset;
  }

  .result-value {
    font-size: 22px;
  }

  .chart {
    height: 280px;
  }

  .cell-val {
    font-size: 12px;
  }

  .preset-section {
    flex-direction: column;
    align-items: flex-start;
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

  .input-card {
    padding: 16px;
  }

  .result-item {
    min-width: 100%;
  }

  .result-value {
    font-size: 20px;
  }

  .chart {
    height: 240px;
  }
}
</style>

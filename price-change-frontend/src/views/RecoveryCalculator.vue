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

const tableData = computed(() => {
  const data = []
  for (let i = 5; i <= 95; i += 5) {
    const gain = (i / (100 - i)) * 100
    data.push({
      decline: i,
      gain: gain.toFixed(2)
    })
  }
  return data
})

const handleInput = (val: number | string) => {
  const num = Number(val)
  if (num < 0) declinePercent.value = 0
  else if (num > 99.9) declinePercent.value = 99.9
}
</script>

<template>
  <div class="recovery-page">
    <div class="container">
      <h1>回本计算器</h1>
      <p class="subtitle">输入下跌百分之多少，计算需要上涨百分之多少才能回本</p>

      <div class="calculator-box">
        <div class="input-group">
          <label>下跌百分比</label>
          <div class="input-wrapper">
            <el-input-number
              v-model="declinePercent"
              :min="0"
              :max="99.9"
              :step="0.01"
              :precision="2"
              placeholder="请输入下跌百分比"
              @input="handleInput"
            />
            <span class="unit">%</span>
          </div>
        </div>

        <div class="result" v-if="requiredGain !== null">
          <div class="result-label">需要上涨</div>
          <div class="result-value">{{ requiredGain }}%</div>
          <div class="result-hint">才能回本</div>
        </div>
      </div>

      <div class="table-section">
        <h2>对照表</h2>
        <el-table :data="tableData" style="width: 100%" size="large">
          <el-table-column prop="decline" label="下跌" align="center">
            <template #default="{ row }">
              <span class="decline-value">{{ row.decline }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="gain" label="需要上涨" align="center">
            <template #default="{ row }">
              <span class="gain-value">{{ row.gain }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recovery-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}
.container {
  max-width: 600px;
  margin: 0 auto;
}
h1 {
  color: #fff;
  text-align: center;
  font-size: 32px;
  margin-bottom: 8px;
}
.subtitle {
  color: rgba(255,255,255,0.8);
  text-align: center;
  margin-bottom: 32px;
}
.calculator-box {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.input-group {
  margin-bottom: 24px;
}
.input-group label {
  display: block;
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.unit {
  font-size: 18px;
  color: #666;
}
.result {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  color: #fff;
}
.result-label {
  font-size: 16px;
  margin-bottom: 8px;
}
.result-value {
  font-size: 48px;
  font-weight: bold;
}
.result-hint {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 8px;
}
.table-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.table-section h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 16px;
  text-align: center;
}
.decline-value {
  color: #f5576c;
  font-weight: bold;
}
.gain-value {
  color: #67c23a;
  font-weight: bold;
}
</style>
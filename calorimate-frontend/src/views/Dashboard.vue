<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>今日卡路里摄入</span>
              <el-tag :type="caloriesTagType">{{ caloriesTagText }}</el-tag>
            </div>
          </template>
          <div ref="caloriesChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>今日营养素</span>
          </template>
          <div ref="nutrientChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日饮食记录</span>
              <el-button type="primary" size="small" @click="goToDietLog">
                <el-icon><Plus /></el-icon> 添加记录
              </el-button>
            </div>
          </template>
          <div v-for="(records, mealType) in dailyData.meals" :key="mealType" class="meal-section">
            <div class="meal-header">
              <span class="meal-type">{{ mealType }}</span>
              <span class="meal-calories">{{ getMealCalories(records) }} kcal</span>
            </div>
            <el-table :data="records" size="small" :show-header="false" empty-text="暂无记录">
              <el-table-column prop="foodName" label="食物" />
              <el-table-column prop="servings" label="份数" width="80">
                <template #default="{ row }">{{ row.servings }} 份</template>
              </el-table-column>
              <el-table-column prop="calories" label="卡路里" width="100">
                <template #default="{ row }">{{ row.calories }} kcal</template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" style="width: 100%" @click="goToDietLog">
              <el-icon><Plus /></el-icon> 记录饮食
            </el-button>
            <el-button size="large" style="width: 100%; margin-top: 12px" @click="router.push('/stats')">
              <el-icon><DataAnalysis /></el-icon> 查看统计
            </el-button>
            <el-button size="large" style="width: 100%; margin-top: 12px" @click="router.push('/food')">
              <el-icon><Bowl /></el-icon> 食物数据库
            </el-button>
          </div>

          <el-divider />

          <div class="user-info-card">
            <h4>个人信息</h4>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="身高">{{ userInfo.height }} cm</el-descriptions-item>
              <el-descriptions-item label="体重">{{ userInfo.weight }} kg</el-descriptions-item>
              <el-descriptions-item label="目标">{{ userInfo.targetCalories }} kcal</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()
const caloriesChartRef = ref()
const nutrientChartRef = ref()
let caloriesChart = null
let nutrientChart = null

const dailyData = reactive({
  date: '',
  totalCalories: 0,
  targetCalories: 2000,
  meals: {}
})

const userInfo = reactive({
  height: 0,
  weight: 0,
  targetCalories: 2000
})

const caloriesTagType = computed(() => {
  return dailyData.totalCalories > dailyData.targetCalories ? 'danger' : 'success'
})

const caloriesTagText = computed(() => {
  const diff = dailyData.targetCalories - dailyData.totalCalories
  if (diff > 0) return `还可摄入 ${diff} kcal`
  return `已超标 ${Math.abs(diff)} kcal`
})

const getMealCalories = (records) => {
  return records.reduce((sum, r) => sum + r.calories, 0)
}

const goToDietLog = () => {
  router.push('/diet-log')
}

const loadDailyData = async () => {
  try {
    const res = await request.get('/diet/log')
    Object.assign(dailyData, res.data)
  } catch (e) {}
}

const loadUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    Object.assign(userInfo, res.data)
    dailyData.targetCalories = res.data.targetCalories || 2000
  } catch (e) {}
}

const renderCaloriesChart = () => {
  if (!caloriesChartRef.value) return
  caloriesChart = echarts.init(caloriesChartRef.value)
  const consumed = dailyData.totalCalories
  const target = dailyData.targetCalories
  const remaining = Math.max(0, target - consumed)

  caloriesChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} kcal ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: '{total|' + consumed.toFixed(0) + '}\n{label|已摄入}',
        position: 'center',
        rich: {
          total: { fontSize: 28, fontWeight: 'bold', color: '#333', lineHeight: 36 },
          label: { fontSize: 14, color: '#999', lineHeight: 24 }
        }
      },
      data: [
        { value: consumed, name: '已摄入', itemStyle: { color: consumed > target ? '#f56c6c' : '#409eff' } },
        { value: remaining, name: '剩余', itemStyle: { color: '#e8e8e8' } }
      ]
    }]
  })
}

const renderNutrientChart = async () => {
  if (!nutrientChartRef.value) return
  nutrientChart = echarts.init(nutrientChartRef.value)

  try {
    const res = await request.get('/stats/daily')
    const data = res.data
    nutrientChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}g ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['0%', '65%'],
        center: ['50%', '42%'],
        data: [
          { value: data.totalProtein || 0, name: '蛋白质', itemStyle: { color: '#409eff' } },
          { value: data.totalFat || 0, name: '脂肪', itemStyle: { color: '#e6a23c' } },
          { value: data.totalCarbs || 0, name: '碳水', itemStyle: { color: '#67c23a' } }
        ],
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
      }]
    })
  } catch (e) {}
}

const handleResize = () => {
  caloriesChart?.resize()
  nutrientChart?.resize()
}

onMounted(async () => {
  await Promise.all([loadDailyData(), loadUserInfo()])
  await nextTick()
  renderCaloriesChart()
  renderNutrientChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  caloriesChart?.dispose()
  nutrientChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.meal-section {
  margin-bottom: 16px;
}
.meal-header {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}
.meal-type {
  font-weight: 600;
  color: #333;
}
.meal-calories {
  color: #409eff;
  font-weight: 500;
}
.quick-actions {
  display: flex;
  flex-direction: column;
}
.user-info-card h4 {
  margin-bottom: 12px;
  color: #333;
}
</style>

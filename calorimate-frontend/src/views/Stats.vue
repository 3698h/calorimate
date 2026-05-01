<template>
  <div class="stats-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-radio-group v-model="viewType" @change="handleViewChange">
            <el-radio-button label="daily">每日</el-radio-button>
            <el-radio-button label="weekly">每周</el-radio-button>
            <el-radio-button label="monthly">每月</el-radio-button>
          </el-radio-group>
          <div>
            <el-date-picker
              v-if="viewType === 'daily'"
              v-model="queryDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadStats"
            />
            <el-date-picker
              v-if="viewType === 'weekly'"
              v-model="queryDate"
              type="week"
              format="YYYY 第 ww 周"
              value-format="YYYY-MM-DD"
              @change="loadStats"
            />
            <el-date-picker
              v-if="viewType === 'monthly'"
              v-model="queryMonth"
              type="month"
              format="YYYY-MM"
              value-format="YYYY-MM"
              @change="loadStats"
            />
          </div>
        </div>
      </template>

      <el-row :gutter="20" v-if="viewType === 'daily'">
        <el-col :span="12">
          <div ref="dailyRingChartRef" style="height: 350px"></div>
        </el-col>
        <el-col :span="12">
          <div ref="dailyMealChartRef" style="height: 350px"></div>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-if="viewType === 'weekly'">
        <el-col :span="16">
          <div ref="weeklyBarChartRef" style="height: 400px"></div>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <h4>本周概览</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="总摄入">{{ weeklyData.totalCalories }} kcal</el-descriptions-item>
              <el-descriptions-item label="日均摄入">{{ weeklyData.avgCalories }} kcal</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="viewType === 'monthly'">
        <el-row :gutter="20">
          <el-col :span="16">
            <div ref="monthlyLineChartRef" style="height: 400px"></div>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never">
              <h4>本月概览</h4>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="总摄入">{{ monthlyData.totalCalories }} kcal</el-descriptions-item>
                <el-descriptions-item label="日均摄入">{{ monthlyData.avgCalories }} kcal</el-descriptions-item>
                <el-descriptions-item label="达标天数">
                  <el-tag type="success">{{ monthlyData.daysOnTarget }} 天</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="超标天数">
                  <el-tag type="danger">{{ monthlyData.daysOverTarget }} 天</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const viewType = ref('daily')
const queryDate = ref(new Date().toISOString().split('T')[0])
const queryMonth = ref(new Date().toISOString().slice(0, 7))

const dailyRingChartRef = ref()
const dailyMealChartRef = ref()
const weeklyBarChartRef = ref()
const monthlyLineChartRef = ref()

let dailyRingChart = null
let dailyMealChart = null
let weeklyBarChart = null
let monthlyLineChart = null

const weeklyData = reactive({ totalCalories: 0, avgCalories: 0, dailyList: [] })
const monthlyData = reactive({ totalCalories: 0, avgCalories: 0, daysOnTarget: 0, daysOverTarget: 0, dailyList: [] })

const disposeAllCharts = () => {
  dailyRingChart?.dispose()
  dailyMealChart?.dispose()
  weeklyBarChart?.dispose()
  monthlyLineChart?.dispose()
  dailyRingChart = null
  dailyMealChart = null
  weeklyBarChart = null
  monthlyLineChart = null
}

const loadStats = async () => {
  if (viewType.value === 'daily') {
    await loadDailyStats()
  } else if (viewType.value === 'weekly') {
    await loadWeeklyStats()
  } else {
    await loadMonthlyStats()
  }
}

const loadDailyStats = async () => {
  try {
    const res = await request.get('/stats/daily', { params: { date: queryDate.value } })
    const data = res.data
    await nextTick()

    dailyRingChart = echarts.init(dailyRingChartRef.value)
    const consumed = data.totalCalories || 0
    const target = data.targetCalories || 2000
    const remaining = Math.max(0, target - consumed)

    dailyRingChart.setOption({
      title: { text: '卡路里摄入', left: 'center', top: 10 },
      tooltip: { trigger: 'item', formatter: '{b}: {c} kcal ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '65%'], center: ['50%', '50%'],
        label: {
          show: true, position: 'center',
          formatter: '{total|' + consumed.toFixed(0) + '}\n{label|已摄入}',
          rich: {
            total: { fontSize: 26, fontWeight: 'bold', color: '#333', lineHeight: 34 },
            label: { fontSize: 13, color: '#999', lineHeight: 22 }
          }
        },
        data: [
          { value: consumed, name: '已摄入', itemStyle: { color: '#409eff' } },
          { value: remaining, name: '剩余', itemStyle: { color: '#e8e8e8' } }
        ]
      }]
    })

    await nextTick()
    dailyMealChart = echarts.init(dailyMealChartRef.value)
    const mealData = data.mealCalories || {}
    dailyMealChart.setOption({
      title: { text: '各餐次占比', left: 'center', top: 10 },
      tooltip: { trigger: 'item', formatter: '{b}: {c} kcal ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: '60%', center: ['50%', '50%'],
        data: Object.entries(mealData).map(([name, value]) => ({
          name, value: value || 0
        })),
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
      }]
    })
  } catch (e) {}
}

const loadWeeklyStats = async () => {
  try {
    const res = await request.get('/stats/weekly', { params: { date: queryDate.value } })
    Object.assign(weeklyData, res.data)
    await nextTick()

    weeklyBarChart = echarts.init(weeklyBarChartRef.value)
    const dates = weeklyData.dailyList.map(d => d.date.slice(5))
    const calories = weeklyData.dailyList.map(d => d.calories)

    weeklyBarChart.setOption({
      title: { text: '本周卡路里摄入', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', name: 'kcal' },
      series: [{
        type: 'bar', data: calories, barWidth: '40%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' }
          ])
        }
      }]
    })
  } catch (e) {}
}

const loadMonthlyStats = async () => {
  try {
    const res = await request.get('/stats/monthly', { params: { month: queryMonth.value } })
    Object.assign(monthlyData, res.data)
    await nextTick()

    monthlyLineChart = echarts.init(monthlyLineChartRef.value)
    const dates = monthlyData.dailyList.map(d => d.date.slice(8))
    const calories = monthlyData.dailyList.map(d => d.calories)

    monthlyLineChart.setOption({
      title: { text: '本月每日卡路里摄入', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates, axisLabel: { interval: 2 } },
      yAxis: { type: 'value', name: 'kcal' },
      series: [{
        type: 'line', data: calories, smooth: true, areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' }
      }]
    })
  } catch (e) {}
}

const handleViewChange = () => {
  disposeAllCharts()
  nextTick(() => loadStats())
}

const handleResize = () => {
  dailyRingChart?.resize()
  dailyMealChart?.resize()
  weeklyBarChart?.resize()
  monthlyLineChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  disposeAllCharts()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
h4 {
  margin-bottom: 16px;
  color: #333;
}
</style>

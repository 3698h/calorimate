<template>
  <div class="ai-page">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🤖 AI 饮食解析</span>
              <el-tag type="info">自然语言输入</el-tag>
            </div>
          </template>

          <div class="input-section">
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="描述你吃了什么，例如：中午吃了一碗米饭、一份红烧肉、一杯可乐"
            />
            <div class="input-actions">
              <el-select v-model="mealType" placeholder="选择餐次（可选）" clearable style="width: 160px">
                <el-option v-for="mt in mealTypes" :key="mt" :label="mt" :value="mt" />
              </el-select>
              <el-button type="primary" :loading="parsing" @click="handleParse">
                <el-icon><MagicStick /></el-icon> AI 解析
              </el-button>
            </div>
          </div>

          <div v-if="parseResult" class="result-section">
            <el-divider content-position="left">解析结果</el-divider>
            <el-card shadow="never" class="result-card">
              <div class="result-header">
                <el-tag type="success" size="large">{{ parseResult.mealType || '未识别餐次' }}</el-tag>
                <span class="total-calories">总计：{{ parseResult.totalCalories }} kcal</span>
              </div>
              <el-table :data="parseResult.foods" size="small" style="margin-top: 12px">
                <el-table-column prop="name" label="食物" />
                <el-table-column prop="amount" label="份量" width="120" />
                <el-table-column prop="calories" label="卡路里(kcal)" width="120" />
              </el-table>
              <div class="save-action">
                <el-button type="success" :loading="saving" @click="handleSave">
                  <el-icon><Check /></el-icon> 一键保存为饮食记录
                </el-button>
                <el-button @click="parseResult = null">清空</el-button>
              </div>
            </el-card>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>💡 AI 营养建议</span>
              <el-button type="primary" size="small" :loading="adviceLoading" @click="handleAdvice">
                获取建议
              </el-button>
            </div>
          </template>

          <div v-if="advice" class="advice-section">
            <div class="advice-item">
              <h4>📊 今日评价</h4>
              <p>{{ advice.evaluation }}</p>
            </div>
            <el-divider />
            <div class="advice-item">
              <h4>🎯 达标分析</h4>
              <p>{{ advice.onTarget }}</p>
            </div>
            <el-divider />
            <div class="advice-item">
              <h4>📋 明天建议</h4>
              <p>{{ advice.suggestion }}</p>
            </div>
          </div>
          <el-empty v-else description="点击「获取建议」查看 AI 营养分析" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const mealTypes = ['早餐', '午餐', '晚餐', '加餐']
const userInput = ref('')
const mealType = ref('')
const parsing = ref(false)
const saving = ref(false)
const adviceLoading = ref(false)
const parseResult = ref(null)
const advice = ref(null)

const handleParse = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入饮食描述')
    return
  }
  parsing.value = true
  try {
    const res = await request.post('/ai/parse', {
      userInput: userInput.value,
      mealType: mealType.value || null
    })
    parseResult.value = res.data
    ElMessage.success('解析完成')
  } catch (e) {
    // handled by interceptor
  } finally {
    parsing.value = false
  }
}

const handleSave = async () => {
  if (!parseResult.value || !parseResult.value.foods || parseResult.value.foods.length === 0) {
    ElMessage.warning('没有可保存的食物')
    return
  }
  saving.value = true
  try {
    const foodsRes = await request.get('/food/list', { params: { size: 100 } })
    const allFoods = foodsRes.data.records
    const foodMap = {}
    allFoods.forEach(f => { foodMap[f.name] = f })

    let savedCount = 0
    for (const foodItem of parseResult.value.foods) {
      const matchedFood = foodMap[foodItem.name]
      if (matchedFood) {
        await request.post('/diet/log', {
          foodId: matchedFood.id,
          mealType: parseResult.value.mealType || mealType.value || '午餐',
          servings: 1,
          logDate: new Date().toISOString().split('T')[0]
        })
        savedCount++
      }
    }

    if (savedCount > 0) {
      ElMessage.success(`成功保存 ${savedCount} 条饮食记录`)
      parseResult.value = null
      userInput.value = ''
    } else {
      ElMessage.warning('未在食物数据库中找到匹配的食物，请手动添加')
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

const handleAdvice = async () => {
  adviceLoading.value = true
  try {
    const res = await request.post('/ai/advice')
    advice.value = res.data
    ElMessage.success('建议获取成功')
  } catch (e) {
    // handled by interceptor
  } finally {
    adviceLoading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.input-section {
  margin-bottom: 12px;
}
.input-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}
.result-section {
  margin-top: 16px;
}
.result-card {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
}
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.total-calories {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}
.save-action {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.advice-section {
  padding: 8px 0;
}
.advice-item h4 {
  color: #333;
  margin-bottom: 8px;
  font-size: 15px;
}
.advice-item p {
  color: #666;
  line-height: 1.8;
  font-size: 14px;
}
</style>

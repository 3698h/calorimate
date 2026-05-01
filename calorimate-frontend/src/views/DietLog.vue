<template>
  <div class="diet-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-date-picker
              v-model="currentDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadDietLogs"
            />
            <el-tag style="margin-left: 12px" type="info">
              总摄入：{{ dailyData.totalCalories || 0 }} kcal
            </el-tag>
          </div>
          <el-button type="primary" @click="openAddDialog()">
            <el-icon><Plus /></el-icon> 添加记录
          </el-button>
        </div>
      </template>

      <div v-for="mealType in mealTypes" :key="mealType" class="meal-section">
        <div class="meal-header">
          <span class="meal-title">{{ mealType }}</span>
          <span class="meal-calories">{{ getMealCalories(mealType) }} kcal</span>
        </div>
        <el-table :data="dailyData.meals?.[mealType] || []" empty-text="暂无记录">
          <el-table-column prop="foodName" label="食物名称" />
          <el-table-column prop="servings" label="份数" width="100">
            <template #default="{ row }">{{ row.servings }} 份</template>
          </el-table-column>
          <el-table-column prop="calories" label="卡路里" width="120">
            <template #default="{ row }">{{ row.calories }} kcal</template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="openEditDialog(row, mealType)">编辑</el-button>
              <el-popconfirm title="确定删除该记录？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '添加饮食记录'" width="500px">
      <el-form ref="logFormRef" :model="logForm" :rules="logRules" label-width="80px">
        <el-form-item label="食物" prop="foodId">
          <el-select
            v-model="logForm.foodId"
            filterable
            remote
            :remote-method="searchFood"
            placeholder="搜索食物"
            style="width: 100%"
          >
            <el-option
              v-for="food in foodOptions"
              :key="food.id"
              :label="`${food.name} (${food.calories}kcal/${food.unit})`"
              :value="food.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="餐次" prop="mealType">
          <el-radio-group v-model="logForm.mealType">
            <el-radio-button v-for="mt in mealTypes" :key="mt" :label="mt">{{ mt }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="份数" prop="servings">
          <el-input-number v-model="logForm.servings" :min="0.1" :max="10" :step="0.5" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const mealTypes = ['早餐', '午餐', '晚餐', '加餐']
const currentDate = ref(new Date().toISOString().split('T')[0])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const logFormRef = ref()
const editId = ref(null)

const dailyData = reactive({
  date: '',
  totalCalories: 0,
  meals: {}
})

const logForm = reactive({
  foodId: null,
  mealType: '早餐',
  servings: 1
})

const foodOptions = ref([])

const logRules = {
  foodId: [{ required: true, message: '请选择食物', trigger: 'change' }],
  mealType: [{ required: true, message: '请选择餐次', trigger: 'change' }],
  servings: [{ required: true, message: '请输入份数', trigger: 'blur' }]
}

const getMealCalories = (mealType) => {
  const records = dailyData.meals?.[mealType] || []
  return records.reduce((sum, r) => sum + r.calories, 0)
}

const loadDietLogs = async () => {
  try {
    const res = await request.get('/diet/log', { params: { date: currentDate.value } })
    Object.assign(dailyData, res.data)
  } catch (e) {}
}

const searchFood = async (query) => {
  if (!query) return
  try {
    const res = await request.get('/food/list', { params: { name: query, size: 20 } })
    foodOptions.value = res.data.records
  } catch (e) {}
}

const loadAllFoods = async () => {
  try {
    const res = await request.get('/food/list', { params: { size: 100 } })
    foodOptions.value = res.data.records
  } catch (e) {}
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  logForm.foodId = null
  logForm.mealType = '早餐'
  logForm.servings = 1
  dialogVisible.value = true
  loadAllFoods()
}

const openEditDialog = (row, mealType) => {
  isEdit.value = true
  editId.value = row.id
  logForm.foodId = row.foodId
  logForm.mealType = mealType
  logForm.servings = row.servings
  dialogVisible.value = true
  loadAllFoods()
}

const handleSubmit = async () => {
  await logFormRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/diet/log/${editId.value}`, { ...logForm, logDate: currentDate.value })
      ElMessage.success('修改成功')
    } else {
      await request.post('/diet/log', { ...logForm, logDate: currentDate.value })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadDietLogs()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/diet/log/${id}`)
    ElMessage.success('删除成功')
    loadDietLogs()
  } catch (e) {}
}

onMounted(() => {
  loadDietLogs()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
}
.meal-section {
  margin-bottom: 24px;
}
.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 2px solid #409eff;
  margin-bottom: 10px;
}
.meal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.meal-calories {
  color: #409eff;
  font-weight: 500;
}
</style>

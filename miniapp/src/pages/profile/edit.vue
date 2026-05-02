<template>
  <view class="edit-page">
    <view v-if="loading" class="loading-wrap">
      <text>加载中...</text>
    </view>
    <view v-else-if="loadError" class="error-wrap">
      <text class="error-text">{{ loadError }}</text>
      <button class="btn-retry" @tap="loadProfile">重试</button>
    </view>
    <block v-else>
      <view class="form-section card">
        <view class="form-item">
          <text class="form-label">性别</text>
          <view class="gender-row">
            <view
              :class="['gender-opt', { active: form.gender === '男' }]"
              @tap="form.gender = '男'"
            >♂ 男</view>
            <view
              :class="['gender-opt', { active: form.gender === '女' }]"
              @tap="form.gender = '女'"
            >♀ 女</view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">出生日期</text>
          <picker mode="date" :value="form.birthday" :end="today" @change="onBirthdayChange">
            <text class="form-value">{{ form.birthday || '请选择' }}</text>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">年龄</text>
          <input
            class="form-input"
            type="number"
            v-model="form.age"
            placeholder="请输入年龄"
            :maxlength="3"
          />
        </view>

        <view class="form-item">
          <text class="form-label">身高 (cm)</text>
          <input
            class="form-input"
            type="digit"
            v-model="form.height"
            placeholder="请输入身高"
          />
        </view>

        <view class="form-item">
          <text class="form-label">体重 (kg)</text>
          <input
            class="form-input"
            type="digit"
            v-model="form.weight"
            placeholder="请输入当前体重"
          />
        </view>

        <view class="form-item">
          <text class="form-label">目标体重 (kg)</text>
          <input
            class="form-input"
            type="digit"
            v-model="form.targetWeight"
            placeholder="请输入目标体重"
          />
        </view>

        <view class="form-item">
          <text class="form-label">活动量</text>
          <picker :range="activityLevelOptions" :range-key="'label'" @change="onActivityLevelChange">
            <text class="form-value">{{ activityLevelLabel || '请选择' }}</text>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">目标</text>
          <picker :range="goalOptions" :range-key="'label'" @change="onGoalChange">
            <text class="form-value">{{ goalLabel || '请选择' }}</text>
          </picker>
        </view>
      </view>

      <view class="save-area">
        <button class="btn-primary" :loading="saving" @tap="handleSave">保存</button>
      </view>
    </block>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Taro from '@tarojs/taro'
import { userApi } from '../../api'

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const today = new Date().toISOString().substring(0, 10)

const form = reactive({
  gender: '男',
  birthday: '',
  age: '',
  height: '',
  weight: '',
  targetWeight: '',
  activityLevel: '',
  goal: '',
})

const activityLevelOptions = [
  { value: 'sedentary', label: '久坐不动' },
  { value: 'light', label: '轻度活动' },
  { value: 'moderate', label: '中度活动' },
  { value: 'active', label: '高强度活动' },
]

const goalOptions = [
  { value: 'lose', label: '减重' },
  { value: 'maintain', label: '维持体重' },
  { value: 'gain', label: '增重' },
]

const activityLevelLabel = computed(() => {
  const opt = activityLevelOptions.find(o => o.value === form.activityLevel)
  return opt ? opt.label : ''
})

const goalLabel = computed(() => {
  const opt = goalOptions.find(o => o.value === form.goal)
  return opt ? opt.label : ''
})

const onActivityLevelChange = (e: any) => {
  form.activityLevel = activityLevelOptions[e.detail.value].value
}

const onGoalChange = (e: any) => {
  form.goal = goalOptions[e.detail.value].value
}

const onBirthdayChange = (e: any) => {
  form.birthday = e.detail.value
}

const loadProfile = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const res = await userApi.getProfile()
    const d = res.data || {}
    form.gender = d.gender || '男'
    form.birthday = d.birthday || ''
    form.age = d.age ? String(d.age) : ''
    form.height = d.height ? String(d.height) : ''
    form.weight = d.weight ? String(d.weight) : ''
    form.targetWeight = d.targetWeight ? String(d.targetWeight) : ''
    form.activityLevel = d.activityLevel || ''
    form.goal = d.goal || ''
  } catch (e: any) {
    const msg = e?.message || e?.errMsg || '加载个人资料失败，请稍后重试'
    loadError.value = msg
    console.error('[edit-profile] loadProfile error:', e)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!form.height || !form.weight) {
    Taro.showToast({ title: '请填写身高和体重', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await userApi.updateProfile({
      gender: form.gender,
      birthday: form.birthday,
      age: form.age ? Number(form.age) : null,
      height: Number(form.height),
      weight: Number(form.weight),
      targetWeight: form.targetWeight ? Number(form.targetWeight) : null,
      activityLevel: form.activityLevel || null,
      goal: form.goal || null,
    })
    try {
      const targetRes = await userApi.getDailyTarget()
      if (targetRes.data?.targetCalories) {
        Taro.setStorageSync('cachedTargetCalories', targetRes.data.targetCalories)
      }
    } catch (_) {}
    Taro.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => Taro.navigateBack(), 1200)
  } catch (e: any) {
    Taro.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onMounted(() => loadProfile())
</script>

<style lang="scss">
.edit-page {
  min-height: 100vh;
  background: #F8FBF8;
  padding: 32rpx 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.loading-wrap, .error-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40rpx;
}

.loading-wrap text {
  font-size: 30rpx;
  color: #7F8C8D;
}

.error-wrap .error-text {
  font-size: 30rpx;
  color: #E74C3C;
  margin-bottom: 32rpx;
  text-align: center;
}

.error-wrap .btn-retry {
  background: #2ECC71;
  color: #FFFFFF;
  border-radius: 999rpx;
  padding: 20rpx 80rpx;
  font-size: 30rpx;
  border: none;
}

.form-section {
  background: #FFFFFF;
  padding: 0;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 24px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  font-size: 30px;
  color: #2C3E50;
  flex-shrink: 0;
  width: 180px;
}

.form-value {
  font-size: 28px;
  color: #7F8C8D;
  text-align: right;
  flex: 1;
}

.form-input {
  flex: 1;
  text-align: right;
  font-size: 28px;
  color: #2C3E50;
}

.gender-row {
  display: flex;
  gap: 16px;
}

.gender-opt {
  padding: 12px 32px;
  border-radius: 32px;
  font-size: 28px;
  color: #7F8C8D;
  background: #F8FBF8;
  transition: all 0.2s;
}

.gender-opt.active {
  background: #2ECC71;
  color: #fff;
}

.save-area {
  padding: 48px 24px;
}
</style>

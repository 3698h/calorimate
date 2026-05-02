<template>
  <view class="index-page">
    <PrivacyPopup @agreed="onPrivacyAgreed" />

    <view v-if="status === 'loading'" class="state-wrap loading-wrap fade-in">
      <view class="loading-spinner" />
      <text class="state-text">正在同步数据...</text>
    </view>

    <view v-else-if="status === 'error'" class="state-wrap error-wrap fade-in">
      <text class="state-icon">📶</text>
      <text class="state-text">{{ errorMsg }}</text>
      <button class="btn-retry" hover-class="btn-hover" @tap="loadData">重新加载</button>
    </view>

    <block v-else>
      <view class="content-fade-in">
        <view v-if="useDefaultTarget" class="default-target-tip">
          <text class="tip-icon">💡</text>
          <text class="tip-text">使用默认目标（完善个人信息可获取个性化推荐）</text>
        </view>

        <view class="header-card card" hover-class="card-hover">
          <view class="user-info">
            <image class="avatar" :src="avatarUrl" mode="aspectFill" />
            <view class="info-content">
              <view class="greeting">
                <text class="greeting-text">{{ greeting }}，</text>
                <text class="greeting-name">{{ nickname }}</text>
              </view>
              <view class="target-badge">
                <text class="target-label">今日目标</text>
                <text class="target-num num-font">{{ targetCal }}</text>
                <text class="target-label">千卡</text>
              </view>
            </view>
          </view>
          <view class="progress-mini" :style="miniRingStyle">
            <view class="progress-inner">
              <text class="percent-num num-font">{{ intakePercent }}</text>
              <text class="percent-sign">%</text>
            </view>
          </view>
        </view>

        <view class="ring-card card" hover-class="card-hover">
          <view class="ring-header">
            <text class="ring-title">热量概览</text>
            <text class="ring-date">{{ todayStr }}</text>
          </view>
          <view class="ring-wrap bounce-in">
            <view class="ring-track" :style="ringStyle">
              <view class="ring-center">
                <text class="ring-label">已摄入</text>
                <text class="ring-value num-font">{{ todayCal }}</text>
                <text class="ring-unit">千卡</text>
              </view>
            </view>
          </view>
          <text class="ring-remain">剩余 <text class="remain-num num-font">{{ remainingCal }}</text> 千卡</text>
          <view class="ring-summary">
            <view class="summary-item">
              <view class="summary-dot dot-eaten" />
              <text class="summary-label">已摄入</text>
              <text class="summary-value num-font">{{ todayCal }}</text>
              <text class="summary-unit">千卡</text>
            </view>
            <view class="summary-divider" />
            <view class="summary-item">
              <view class="summary-dot dot-remain" />
              <text class="summary-label">剩余</text>
              <text class="summary-value num-font">{{ remainingCal }}</text>
              <text class="summary-unit">千卡</text>
            </view>
          </view>
        </view>

        <view class="section-header">
          <text class="section-title">今日记录</text>
          <text class="section-count" v-if="records.length > 0">{{ records.length }}条</text>
        </view>

        <view v-if="records.length > 0" class="record-list">
          <view
            v-for="(item, index) in records"
            :key="item.id || index"
            class="record-item card"
            hover-class="item-hover"
            @tap="onRecordTap(item)"
          >
            <view class="record-icon">{{ getMealIcon(item.mealType) }}</view>
            <view class="record-main">
              <text class="record-name">{{ item.foodName }}</text>
              <text class="record-meta">{{ getMealLabel(item.mealType) }} · {{ item.servings }}份</text>
            </view>
            <view class="record-right">
              <text class="record-cal num-font">{{ item.calories }}</text>
              <text class="record-unit">千卡</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-state card">
          <text class="empty-emoji bounce-in">🥗</text>
          <text class="empty-title">今天还没记录呢</text>
          <text class="empty-desc">来拍第一张食物照片吧，AI 会自动帮你算热量哦～</text>
        </view>

        <view class="ad-banner-wrap card" v-if="status === 'done' && !isVip">
          <ad unit-id="adunit-xxxxxxxx" type="banner"></ad>
        </view>

        <view class="action-cards">
          <button class="action-btn quick-btn" hover-class="btn-hover" @tap="goFood">
            <view class="btn-icon-wrap quick-icon-bg">
              <text class="btn-icon">✏️</text>
            </view>
            <text class="btn-label">快速记录</text>
            <text class="btn-hint">手动搜索添加</text>
          </button>
          <button class="action-btn camera-btn" hover-class="btn-hover" @tap="goCamera">
            <view class="btn-icon-wrap camera-icon-bg">
              <text class="btn-icon">📸</text>
            </view>
            <text class="btn-label">AI 拍照识别</text>
            <text class="btn-hint">一键智能识别</text>
          </button>
        </view>
      </view>
    </block>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import PrivacyPopup from '../../components/PrivacyPopup.vue'
import { userApi, recordApi } from '../../api'

type LoadStatus = 'loading' | 'done' | 'error'

const status = ref<LoadStatus>('loading')
const errorMsg = ref('')
const nickname = ref('健康达人')
const avatarUrl = ref('https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0')
const todayCal = ref(0)
const targetCal = ref(2000)
const records = ref<any[]>([])
const isVip = ref(false)
const useDefaultTarget = ref(false)
const hasInitialLoad = ref(false)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const intakePercent = computed(() => {
  if (targetCal.value <= 0) return 0
  return Math.min(100, Math.round((todayCal.value / targetCal.value) * 100))
})

const remainingCal = computed(() => Math.max(0, targetCal.value - todayCal.value))

const miniRingStyle = computed(() => {
  const deg = (intakePercent.value / 100) * 360
  return {
    background: `conic-gradient(#2ECC71 ${deg}deg, #E8F5E9 ${deg}deg)`
  }
})

const ringStyle = computed(() => {
  const percent = intakePercent.value
  const deg = (percent / 100) * 360
  return {
    background: `conic-gradient(from -90deg, #2ECC71 0deg, #27AE60 ${deg}deg, #EFF7F0 ${deg}deg, #EFF7F0 360deg)`
  }
})

const getMealIcon = (type: string) => {
  const map: Record<string, string> = { breakfast: '🍳', lunch: '🍱', dinner: '🥘', snack: '🍎' }
  return map[type] || '🍽️'
}

const getMealLabel = (type: string) => {
  const map: Record<string, string> = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
  return map[type] || '其他'
}

const loadData = async () => {
  status.value = 'loading'
  errorMsg.value = ''
  useDefaultTarget.value = false
  try {
    const [profileRes, dietRes, dailyTargetRes] = await Promise.allSettled([
      userApi.getProfile(),
      recordApi.list(),
      userApi.getDailyTarget(),
    ])

    if (profileRes.status === 'fulfilled') {
      nickname.value = profileRes.value.data?.username || '健康达人'
      isVip.value = (profileRes.value.data?.vipLevel || 0) > 0
    }

    if (dailyTargetRes.status === 'fulfilled') {
      targetCal.value = dailyTargetRes.value.data?.targetCalories || 2000
      useDefaultTarget.value = false
    } else {
      targetCal.value = profileRes.status === 'fulfilled'
        ? (profileRes.value.data?.targetCalories || 2000)
        : 2000
      useDefaultTarget.value = true
    }

    if (dietRes.status === 'fulfilled') {
      const mealsMap = dietRes.value.data?.meals || {}
      const allRecords: any[] = []
      Object.keys(mealsMap).forEach((mealType) => {
        const items = mealsMap[mealType] || []
        items.forEach((item: any) => {
          allRecords.push({ ...item, mealType })
        })
      })
      records.value = allRecords
      todayCal.value = dietRes.value.data?.totalCalories || 0
    }

    if (profileRes.status === 'rejected' && dietRes.status === 'rejected') {
      throw new Error('加载失败')
    }

    status.value = 'done'
  } catch (e: any) {
    errorMsg.value = e.message || '加载失败'
    status.value = 'error'
  }
}

const onDeleteRecord = async (item: any, index: number) => {
  const res = await Taro.showModal({
    title: '删除记录',
    content: `确定要删除「${item.foodName}」吗？`,
    confirmColor: '#FF3B30'
  })
  if (!res.confirm) return
  try {
    await recordApi.delete(item.id)
    records.value.splice(index, 1)
    todayCal.value = Math.max(0, todayCal.value - (item.calories || 0))
    Taro.showToast({ title: '已删除', icon: 'success', duration: 1500 })
  } catch (e: any) {
    Taro.showToast({ title: e.message || '删除失败', icon: 'none' })
  }
}

const onRecordTap = async (item: any) => {
  const res = await Taro.showActionSheet({
    itemList: ['删除该记录'],
  })
  if (res.tapIndex === 0) {
    onDeleteRecord(item, records.value.indexOf(item))
  }
}

const goFood = () => Taro.switchTab({ url: '/pages/food/index' })
const goCamera = () => Taro.switchTab({ url: '/pages/camera/index' })

const onPrivacyAgreed = () => {
  if (!Taro.getStorageSync('token')) {
    Taro.redirectTo({ url: '/pages/login/index' })
    return
  }
  loadData()
}

onMounted(() => {
  if (!Taro.getStorageSync('token')) {
    Taro.redirectTo({ url: '/pages/login/index' })
    return
  }
  if (Taro.getStorageSync('privacy_agreed')) {
    loadData()
    hasInitialLoad.value = true
  }
})

useDidShow(() => {
  if (Taro.getStorageSync('token') && Taro.getStorageSync('privacy_agreed') && hasInitialLoad.value) {
    loadData()
  }
})
</script>

<style lang="scss">
$bg-color: #F8FBF8;
$primary: #2ECC71;
$primary-dark: #27AE60;
$accent: #F39C12;
$primary-gradient: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
$text-main: #2C3E50;
$text-secondary: #34495E;
$text-sub: #7F8C8D;
$card-radius: 20rpx;
$card-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04), 0 0 1rpx rgba(0, 0, 0, 0.06);

.index-page {
  min-height: 100vh;
  background-color: $bg-color;
  padding: 24rpx 28rpx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 32rpx);
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.num-font {
  font-family: 'DIN Alternate', 'SF Pro Display', 'Helvetica Neue', monospace;
}

.fade-in {
  animation: fadeIn 0.4s ease-out forwards;
}

.content-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

.bounce-in {
  animation: bounceIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounceIn {
  0% { opacity: 0; transform: scale(0.85); }
  100% { opacity: 1; transform: scale(1); }
}

.card-hover {
  transform: scale(0.975);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-hover {
  transform: scale(0.96) !important;
  opacity: 0.85;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.item-hover {
  background-color: #EFF7F0 !important;
  transform: scale(0.985);
}

.card {
  background: #FFFFFF;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  padding: 32rpx;
  margin-bottom: 20rpx;
  transition: all 0.25s ease;
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;

  .state-icon {
    font-size: 80rpx;
    margin-bottom: 24rpx;
  }

  .state-text {
    font-size: 28rpx;
    color: $text-sub;
    margin-bottom: 32rpx;
  }

  .btn-retry {
    background: $primary-gradient;
    color: #fff;
    font-size: 28rpx;
    font-weight: 600;
    padding: 0 64rpx;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(46, 204, 113, 0.3);

    &::after { border: none; }
  }
}

.default-target-tip {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFF3CD 100%);
  border: 1rpx solid rgba(255, 193, 7, 0.2);
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  margin-bottom: 20rpx;

  .tip-icon {
    font-size: 28rpx;
    flex-shrink: 0;
  }

  .tip-text {
    font-size: 24rpx;
    color: #8D6E00;
    font-weight: 500;
  }
}

.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;

  .user-info {
    display: flex;
    align-items: center;
    gap: 24rpx;

    .avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
      border: 3rpx solid rgba(46, 204, 113, 0.2);
    }

    .info-content {
      display: flex;
      flex-direction: column;
      gap: 10rpx;
    }

    .greeting {
      display: flex;
      align-items: baseline;
      flex-wrap: wrap;

      .greeting-text {
        font-size: 26rpx;
        color: $text-sub;
        font-weight: 400;
      }

      .greeting-name {
        font-size: 32rpx;
        font-weight: 700;
        color: $text-main;
        margin-left: 6rpx;
      }
    }

    .target-badge {
      display: inline-flex;
      align-items: baseline;
      background: linear-gradient(135deg, rgba(46, 204, 113, 0.08) 0%, rgba(46, 204, 113, 0.04) 100%);
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
      border: 1rpx solid rgba(46, 204, 113, 0.12);

      .target-label {
        font-size: 22rpx;
        color: #2ECC71;
        font-weight: 500;
      }

      .target-num {
        font-size: 28rpx;
        color: #2ECC71;
        font-weight: 700;
        margin: 0 6rpx;
      }
    }
  }

  .progress-mini {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .progress-inner {
      width: 68rpx;
      height: 68rpx;
      background: #FFFFFF;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

      .percent-num {
        font-size: 26rpx;
        font-weight: 700;
        color: $text-main;
      }

      .percent-sign {
        font-size: 16rpx;
        color: $text-sub;
        margin-left: 1rpx;
      }
    }
  }
}

.ring-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;

  .ring-header {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .ring-title {
      font-size: 30rpx;
      font-weight: 700;
      color: $text-main;
    }

    .ring-date {
      font-size: 24rpx;
      color: $text-sub;
      font-weight: 500;
    }
  }

  .ring-wrap {
    width: 380rpx;
    height: 380rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .ring-track {
    width: 380rpx;
    height: 380rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .ring-center {
    width: 290rpx;
    height: 290rpx;
    border-radius: 50%;
    background: #FFFFFF;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
  }

  .ring-label {
    font-size: 24rpx;
    color: $text-sub;
    font-weight: 500;
  }

  .ring-value {
    font-size: 72rpx;
    font-weight: 800;
    color: $text-main;
    line-height: 1.1;
  }

  .ring-unit {
    font-size: 24rpx;
    color: $text-sub;
    font-weight: 500;
    margin-top: -2rpx;
  }

  .ring-remain {
    font-size: 26rpx;
    color: $text-sub;
    margin: 20rpx 0 8rpx;

    .remain-num {
      font-size: 30rpx;
      font-weight: 700;
      color: $primary;
    }
  }

  .ring-summary {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    padding-top: 16rpx;
    border-top: 1rpx solid #F2F2F7;

    .summary-item {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10rpx;
    }

    .summary-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      flex-shrink: 0;

      &.dot-eaten {
        background: $primary-gradient;
      }

      &.dot-remain {
        background: #E8F5E9;
        border: 2rpx solid rgba(46, 204, 113, 0.3);
      }
    }

    .summary-label {
      font-size: 24rpx;
      color: $text-sub;
      font-weight: 500;
    }

    .summary-value {
      font-size: 32rpx;
      font-weight: 700;
      color: $text-main;
    }

    .summary-unit {
      font-size: 22rpx;
      color: $text-sub;
      font-weight: 400;
    }

    .summary-divider {
      width: 1rpx;
      height: 48rpx;
      background: #E5E5EA;
      flex-shrink: 0;
    }
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 32rpx 4rpx 20rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: 700;
    color: $text-main;
    letter-spacing: 0.5rpx;
  }

  .section-count {
    font-size: 24rpx;
    color: $text-sub;
    font-weight: 500;
    background: #F2F2F7;
    padding: 4rpx 16rpx;
    border-radius: 16rpx;
  }
}

.record-list {
  .record-item {
    display: flex;
    align-items: center;
    padding: 24rpx 28rpx;

    .record-icon {
      width: 76rpx;
      height: 76rpx;
      background: linear-gradient(135deg, #F8FBF8 0%, #E8F5E9 100%);
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 38rpx;
      margin-right: 20rpx;
      flex-shrink: 0;
    }

    .record-main {
      flex: 1;
      min-width: 0;

      .record-name {
        font-size: 30rpx;
        font-weight: 600;
        color: $text-main;
        display: block;
        margin-bottom: 6rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .record-meta {
        font-size: 24rpx;
        color: $text-sub;
        font-weight: 400;
      }
    }

    .record-right {
      text-align: right;
      flex-shrink: 0;
      margin-left: 16rpx;

      .record-cal {
        font-size: 34rpx;
        font-weight: 700;
        color: $text-main;
      }

      .record-unit {
        font-size: 20rpx;
        color: $text-sub;
        margin-left: 4rpx;
        font-weight: 400;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  text-align: center;

  .empty-emoji {
    font-size: 96rpx;
    margin-bottom: 24rpx;
  }

  .empty-title {
    font-size: 32rpx;
    font-weight: 600;
    color: $text-main;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: $text-sub;
    line-height: 1.6;
    padding: 0 32rpx;
  }
}

.ad-banner-wrap {
  padding: 16rpx;
  overflow: hidden;
}

.action-cards {
  display: flex;
  gap: 20rpx;
  margin-top: 32rpx;
  margin-bottom: 16rpx;

  .action-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 220rpx;
    border-radius: $card-radius;
    border: none;
    background: #FFFFFF;
    box-shadow: $card-shadow;
    padding: 24rpx;
    gap: 10rpx;

    &::after { border: none; }

    .btn-icon-wrap {
      width: 80rpx;
      height: 80rpx;
      border-radius: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-icon {
      font-size: 40rpx;
    }

    .btn-label {
      font-size: 30rpx;
      font-weight: 600;
      color: $text-main;
    }

    .btn-hint {
      font-size: 22rpx;
      color: $text-sub;
      font-weight: 400;
    }

    &.quick-btn {
      .quick-icon-bg {
        background: linear-gradient(135deg, rgba(46, 204, 113, 0.12) 0%, rgba(46, 204, 113, 0.06) 100%);
      }
    }

    &.camera-btn {
      background: $primary-gradient;
      box-shadow: 0 8rpx 28rpx rgba(46, 204, 113, 0.35);

      .camera-icon-bg {
        background: rgba(255, 255, 255, 0.2);
      }

      .btn-label {
        color: #FFFFFF;
      }

      .btn-hint {
        color: rgba(255, 255, 255, 0.75);
      }
    }
  }
}
</style>

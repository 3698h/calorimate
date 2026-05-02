<template>
  <view class="profile-page">
    <PrivacyPopup @agreed="onPrivacyAgreed" />

    <!-- 状态遮罩 -->
    <view v-if="loading" class="state-wrap fade-in">
      <view class="loading-spinner" />
    </view>
    
    <view v-else-if="error" class="state-wrap fade-in">
      <text class="error-text">{{ error }}</text>
      <button class="btn-retry primary-btn" hover-class="btn-hover" @tap="loadData">重新加载</button>
    </view>

    <block v-else>
      <view class="content-fade-in">
        
        <!-- 1. 顶部渐变背景与用户信息 -->
        <view class="top-header-bg">
          <view class="user-info-area" @tap="goEdit">
            <view class="avatar-wrap">
              <image class="avatar" :src="userInfo.avatarUrl || defaultAvatar" mode="aspectFill" />
              <view v-if="isVip" class="vip-crown">👑</view>
            </view>
            <view class="name-wrap">
              <view class="name-line">
                <text class="nickname">{{ userInfo.username || '健康探索者' }}</text>
                <text class="vip-tag" v-if="isVip">VIP尊享</text>
              </view>
              <text class="sub-text">编辑个人资料 ›</text>
            </view>
          </view>
        </view>

        <!-- 2. 悬浮健康数据速览 -->
        <view class="overview-card card float-up">
          <view class="stat-item">
            <view class="stat-val num-font">{{ userInfo.height || '--' }}</view>
            <view class="stat-lab">身高 (cm)</view>
          </view>
          <view class="divider"></view>
          <view class="stat-item">
            <view class="stat-val num-font">{{ userInfo.weight || '--' }}</view>
            <view class="stat-lab">体重 (kg)</view>
          </view>
          <view class="divider"></view>
          <view class="stat-item">
            <view class="stat-val num-font text-primary">{{ userInfo.targetCalories || '--' }}</view>
            <view class="stat-lab">目标 (kcal)</view>
          </view>
        </view>

        <!-- 3. 本周概览卡片 -->
        <view class="section-title">本周概览</view>
        <view class="weekly-card card" hover-class="card-hover">
          <view class="week-half border-right">
            <text class="half-title">日均摄入</text>
            <view class="half-data">
              <text class="data-num num-font text-primary">{{ weekAvgCalories }}</text>
              <text class="data-unit">kcal</text>
            </view>
          </view>
          <view class="week-half">
            <text class="half-title">体重趋势</text>
            <view class="half-data trend-down">
              <text class="data-num num-font">-0.5</text>
              <text class="data-unit">kg ↘️</text>
            </view>
          </view>
        </view>

        <!-- 打卡日历卡片 (优化视觉版) -->
        <view class="section-title">饮食打卡日历</view>
        <view class="calendar-card card">
          <view class="cal-header">
            <view class="cal-nav" hover-class="nav-hover" @tap="changeMonth(-1)">‹</view>
            <text class="cal-month">{{ calYear }}年{{ calMonth + 1 }}月</text>
            <view class="cal-nav" hover-class="nav-hover" @tap="changeMonth(1)">›</view>
          </view>
          <view class="cal-weekdays">
            <text v-for="d in weekLabels" :key="d" class="cal-wd">{{ d }}</text>
          </view>
          <view class="cal-grid">
            <view
              v-for="(cell, i) in calendarCells"
              :key="i"
              :class="['cal-cell', { empty: !cell.day }]"
            >
              <view v-if="cell.day" :class="['cal-day', { active: cell.hasRecord, today: cell.isToday }]">
                <text class="num-font">{{ cell.day }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 4. 功能菜单列表 -->
        <view class="menu-list card">
          <view class="menu-item" hover-class="item-hover" @tap="goVip">
            <view class="menu-icon-wrap bg-gold"><text class="menu-icon">👑</text></view>
            <view class="menu-content border-bottom">
              <text class="menu-label">我的会员</text>
              <view class="menu-right">
                <text class="menu-hint" v-if="isVip">{{ formatExpire(userInfo.vipExpireTime) }} 到期</text>
                <text class="menu-hint highlight" v-else>解锁无限极速体验</text>
                <text class="menu-arrow">›</text>
              </view>
            </view>
          </view>

          <view class="menu-item" hover-class="item-hover">
            <view class="menu-icon-wrap bg-green"><text class="menu-icon">📅</text></view>
            <view class="menu-content border-bottom">
              <text class="menu-label">饮食日历</text>
              <view class="menu-right"><text class="menu-arrow">›</text></view>
            </view>
          </view>

          <view class="menu-item" hover-class="item-hover" @tap="goSettings">
            <view class="menu-icon-wrap bg-gray"><text class="menu-icon">⚙️</text></view>
            <view class="menu-content border-bottom">
              <text class="menu-label">设置</text>
              <view class="menu-right"><text class="menu-arrow">›</text></view>
            </view>
          </view>

          <view class="menu-item" hover-class="item-hover" @tap="feedback">
            <view class="menu-icon-wrap bg-blue"><text class="menu-icon">💬</text></view>
            <view class="menu-content">
              <text class="menu-label">反馈</text>
              <view class="menu-right"><text class="menu-arrow">›</text></view>
            </view>
          </view>
        </view>

        <!-- 5. 退出登录按钮 (纯净文字版) -->
        <view class="logout-area" hover-class="btn-hover" @tap="handleLogout">
          <text class="logout-text">退出当前账号</text>
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

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iI2UwZTBlMCIvPjxjaXJjbGUgY3g9IjUwIiBjeT0iMzgiIHI9IjE4IiBmaWxsPSIjYmRiZGJkIi8+PGVsbGlwcGUgY3g9IjUwIiBjeT0iNzgiIHJ4PSIyOCIgcnk9IjIwIiBmaWxsPSIjYmRiZGJkIi8+PC9zdmc+'

const loading = ref(true)
const error = ref('')
const userInfo = ref<any>({})
const calYear = ref(new Date().getFullYear())
const calMonth = ref(new Date().getMonth())
const recordDates = ref<Set<string>>(new Set())
const weekAvgCalories = ref(0)

const isVip = computed(() => (userInfo.value.vipLevel || 0) > 0)

const weekLabels = ['日', '一', '二', '三', '四', '五', '六']

interface CalCell {
  day: number
  hasRecord: boolean
  isToday: boolean
}

// ...日历逻辑保持不变...
const calendarCells = computed<CalCell[]>(() => {
  const firstDay = new Date(calYear.value, calMonth.value, 1).getDay()
  const daysInMonth = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const today = new Date()
  const cells: CalCell[] = []
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: 0, hasRecord: false, isToday: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${calYear.value}-${String(calMonth.value + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const isToday = today.getFullYear() === calYear.value && today.getMonth() === calMonth.value && today.getDate() === d
    cells.push({ day: d, hasRecord: recordDates.value.has(dateStr), isToday })
  }
  return cells
})

const formatExpire = (t: string) => t ? t.substring(0, 10) : '--'

const changeMonth = (delta: number) => {
  const d = new Date(calYear.value, calMonth.value + delta, 1)
  calYear.value = d.getFullYear()
  calMonth.value = d.getMonth()
  loadCalendarData()
}

// ...数据加载逻辑保持不变...
const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [profileRes, dailyTargetRes] = await Promise.allSettled([
      userApi.getProfile(),
      userApi.getDailyTarget(),
    ])
    if (profileRes.status === 'fulfilled') {
      userInfo.value = profileRes.value.data || {}
    }
    if (dailyTargetRes.status === 'fulfilled' && dailyTargetRes.value.data) {
      const target = dailyTargetRes.value.data.targetCalories
      if (target) {
        userInfo.value = { ...userInfo.value, targetCalories: target }
        Taro.setStorageSync('cachedTargetCalories', target)
      }
    }
    if (!userInfo.value.targetCalories) {
      const cached = Taro.getStorageSync('cachedTargetCalories')
      if (cached) {
        userInfo.value = { ...userInfo.value, targetCalories: cached }
      }
    }
    await Promise.all([loadCalendarData(), loadWeekAvg()])
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const loadCalendarData = async () => {
  const dates = new Set<string>()
  const daysInMonth = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const tasks: Promise<void>[] = []
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${calYear.value}-${String(calMonth.value + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    tasks.push(
      recordApi.daily(dateStr)
        .then(res => { if (res.data && res.data.totalCalories > 0) dates.add(dateStr) })
        .catch(() => {})
    )
  }
  await Promise.all(tasks)
  recordDates.value = dates
}

const loadWeekAvg = async () => {
  let total = 0
  let count = 0
  const today = new Date()
  const tasks: Promise<void>[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    tasks.push(
      recordApi.daily(dateStr)
        .then(res => {
          if (res.data && res.data.totalCalories > 0) { total += res.data.totalCalories; count++; }
        })
        .catch(() => {})
    )
  }
  await Promise.all(tasks)
  weekAvgCalories.value = count > 0 ? Math.round(total / count) : 0
}

const goEdit = () => Taro.navigateTo({ url: '/pages/profile/edit' })
const goVip = () => Taro.navigateTo({ url: '/pages/vip/index' })
const goSettings = () => Taro.showToast({ title: '设置功能开发中', icon: 'none' })

const feedback = () => {
  Taro.showModal({
    title: '反馈',
    content: '如有问题或建议，请联系客服\nsupport@calorimate.com',
    showCancel: false,
    confirmColor: '#2ECC71'
  })
}

const handleLogout = () => {
  Taro.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmColor: '#FA5151',
    success: res => {
      if (res.confirm) {
        Taro.removeStorageSync('token')
        Taro.redirectTo({ url: '/pages/login/index' })
      }
    },
  })
}

const onPrivacyAgreed = () => { loadData() }

onMounted(() => { if (Taro.getStorageSync('privacy_agreed')) loadData() })
useDidShow(() => { if (Taro.getStorageSync('privacy_agreed')) loadData() })
</script>

<style lang="scss">
/* --- 变量定义 --- */
$bg-color: #F8FBF8;
$primary: #2ECC71;
$primary-gradient: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
$text-main: #2C3E50;
$text-sub: #7F8C8D;
$danger: #E74C3C;

.profile-page {
  min-height: 100vh;
  background: $bg-color;
  padding-bottom: env(safe-area-inset-bottom);
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.num-font { font-family: 'DIN Alternate', 'SF Pro Display', Consolas, monospace; }
.text-primary { color: #2ECC71; }

/* 动画效果 */
.fade-in { animation: fadeIn 0.3s ease-out forwards; }
.content-fade-in { animation: fadeIn 0.5s ease-out forwards; }
.float-up { animation: floatUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes floatUp { from { opacity: 0; transform: translateY(40rpx); } to { opacity: 1; transform: translateY(0); } }

/* 交互反馈 */
.card-hover { transform: scale(0.98); transition: transform 0.2s; }
.item-hover { background-color: #F9F9F9 !important; }
.btn-hover { opacity: 0.7; transition: opacity 0.2s; }
.nav-hover { background-color: #F0F0F0; border-radius: 50%; }

/* 卡片基类 */
.card {
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
  margin: 0 32rpx 32rpx;
  padding: 32rpx;
}

/* 小节标题 */
.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-main;
  margin: 48rpx 40rpx 20rpx;
}

/* --- 1. 顶部渐变背景 (Apple 个人页风格) --- */
.top-header-bg {
  background: $primary-gradient;
  padding: 100rpx 40rpx 120rpx; /* 底部预留空间给卡片悬浮 */
  border-radius: 0 0 48rpx 48rpx;

  .user-info-area {
    display: flex;
    align-items: center;
    gap: 32rpx;
  }

  .avatar-wrap {
    position: relative;
    .avatar {
      width: 128rpx; height: 128rpx;
      border-radius: 50%;
      border: 6rpx solid rgba(255, 255, 255, 0.4);
      background: #eee;
    }
    .vip-crown {
      position: absolute;
      top: -16rpx; right: -8rpx;
      width: 48rpx; height: 48rpx;
      background: linear-gradient(135deg, #FFD700, #FFA500);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 28rpx;
      box-shadow: 0 4rpx 12rpx rgba(255, 165, 0, 0.4);
      border: 3rpx solid #FFD700;
    }
  }

  .name-wrap {
    flex: 1;
    .name-line {
      display: flex; align-items: center; gap: 16rpx; margin-bottom: 8rpx;
      .nickname { font-size: 40rpx; font-weight: 700; color: #FFFFFF; }
      .vip-tag {
        font-size: 20rpx; font-weight: 700;
        color: #B8860B; background: linear-gradient(135deg, #FFDF00, #FFB300);
        padding: 4rpx 12rpx; border-radius: 12rpx;
      }
    }
    .sub-text { font-size: 24rpx; color: rgba(255, 255, 255, 0.8); }
  }
}

/* --- 2. 悬浮数据卡片 --- */
.overview-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -80rpx; /* 向上提，形成悬浮 */
  position: relative;
  z-index: 10;
  padding: 40rpx 20rpx;

  .stat-item {
    flex: 1; text-align: center;
    .stat-val { font-size: 44rpx; font-weight: 700; color: $text-main; margin-bottom: 4rpx; }
    .stat-lab { font-size: 22rpx; color: $text-sub; }
  }

  .divider { width: 2rpx; height: 48rpx; background: #F0F2F5; }
}

/* --- 3. 本周概览卡片 --- */
.weekly-card {
  display: flex;
  padding: 40rpx 0;

  .week-half {
    flex: 1;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    
    &.border-right { border-right: 2rpx solid #F0F2F5; }

    .half-title { font-size: 24rpx; color: $text-sub; margin-bottom: 12rpx; }
    .half-data {
      display: flex; align-items: baseline; gap: 6rpx;
      .data-num { font-size: 48rpx; font-weight: 700; color: $text-main; }
      .data-unit { font-size: 24rpx; color: $text-sub; font-weight: 500; }
      
      &.trend-down .data-num { color: #2ECC71; }
      &.trend-up .data-num { color: $danger; }
    }
  }
}

/* --- 4. 优化版日历卡片 --- */
.calendar-card {
  padding: 32rpx 20rpx 40rpx;

  .cal-header {
    display: flex; justify-content: space-between; align-items: center;
    margin-bottom: 32rpx; padding: 0 20rpx;
    
    .cal-month { font-size: 30rpx; font-weight: 600; color: $text-main; }
    .cal-nav { font-size: 40rpx; color: $text-sub; width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; }
  }

  .cal-weekdays {
    display: flex; margin-bottom: 16rpx;
    .cal-wd { flex: 1; text-align: center; font-size: 22rpx; color: $text-sub; font-weight: 500;}
  }

  .cal-grid { display: flex; flex-wrap: wrap; }
  
  .cal-cell {
    width: calc(100% / 7); aspect-ratio: 1;
    display: flex; align-items: center; justify-content: center;
    
    .cal-day {
      width: 60rpx; height: 60rpx;
      display: flex; align-items: center; justify-content: center;
      font-size: 28rpx; border-radius: 50%;
      color: $text-main;
      transition: all 0.2s;

      /* 有记录的日子 */
      &.active { background: rgba(46, 204, 113, 0.15); color: #2ECC71; font-weight: 600; }
      /* 今天 */
      &.today { border: 2rpx solid $primary; color: $primary; font-weight: 700; }
      /* 今天且有记录 */
      &.today.active { background: $primary-gradient; color: #fff; border: none; box-shadow: 0 4rpx 12rpx rgba(46, 204, 113, 0.3);}
    }
  }
}

/* --- 5. 功能菜单列表 --- */
.menu-list {
  padding: 0; overflow: hidden; /* 切掉直角 */

  .menu-item {
    display: flex; align-items: center;
    padding: 0 32rpx;
    background: #fff;
    transition: background 0.2s;
  }

  .menu-icon-wrap {
    width: 56rpx; height: 56rpx;
    border-radius: 16rpx;
    display: flex; align-items: center; justify-content: center;
    margin-right: 24rpx;
    
    .menu-icon { font-size: 32rpx; }
    &.bg-gold { background: rgba(255, 215, 0, 0.15); }
    &.bg-green { background: rgba(46, 204, 113, 0.15); }
    &.bg-gray { background: #F0F2F5; }
    &.bg-blue { background: rgba(10, 132, 255, 0.15); }
  }

  .menu-content {
    flex: 1; display: flex; align-items: center; justify-content: space-between;
    padding: 36rpx 0;
    
    &.border-bottom { border-bottom: 1rpx solid #F0F2F5; }

    .menu-label { font-size: 30rpx; color: $text-main; font-weight: 500; }
    
    .menu-right {
      display: flex; align-items: center; gap: 12rpx;
      .menu-hint { font-size: 24rpx; color: $text-sub; }
      .menu-hint.highlight { color: #FF9500; }
      .menu-arrow { font-size: 36rpx; color: #C7C7CC; margin-top: -4rpx;}
    }
  }
}

/* --- 6. 退出登录 --- */
.logout-area {
  display: flex; justify-content: center; align-items: center;
  padding: 40rpx 0 80rpx;

  .logout-text {
    font-size: 30rpx; font-weight: 500;
    color: $danger;
  }
}
</style>
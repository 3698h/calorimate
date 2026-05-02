<template>
  <view class="camera-page">
    <view v-if="!imageUrl" class="camera-home fade-in">
      <view class="times-badge" :class="{ 'is-vip': isVip }">
        <text v-if="!isVip" class="badge-text">今日免费识别剩余 <text class="highlight">{{ remainFreeTimes }}</text> 次</text>
        <text v-else class="badge-text">👑 会员尊享无限次识别</text>
      </view>

      <view class="viewfinder-wrap" @tap="takePhoto">
        <view class="pulse-ring pulse-1" />
        <view class="pulse-ring pulse-2" />
        <view class="camera-circle">
          <text class="circle-icon">📸</text>
          <text class="circle-title">拍照识别</text>
          <text class="circle-sub">AI 自动计算卡路里</text>
        </view>
      </view>

      <view class="tips-row">
        <view class="tip-item">
          <text class="tip-icon">💡</text>
          <text class="tip-text">光线充足</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">🎯</text>
          <text class="tip-text">画面居中</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">📐</text>
          <text class="tip-text">俯拍最佳</text>
        </view>
      </view>

      <view class="divider">
        <view class="divider-line" />
        <text class="divider-text">OR</text>
        <view class="divider-line" />
      </view>

      <view class="text-input-card">
        <text class="input-title">键盘输入</text>
        <textarea
          class="text-area"
          v-model="textInput"
          placeholder="例如：一碗米饭、一盘西兰花炒虾仁..."
          placeholder-class="ph-color"
          :maxlength="500"
        />
        <button
          class="text-submit-btn"
          :loading="textLoading"
          :disabled="!textInput.trim()"
          @tap="parseText"
        >
          <text v-if="!textLoading">AI 智能解析</text>
        </button>
      </view>
    </view>

    <view v-else class="camera-result">
      <view class="preview-container">
        <view class="corner corner-tl" />
        <view class="corner corner-tr" />
        <view class="corner corner-bl" />
        <view class="corner corner-br" />

        <image class="preview-img" :src="imageUrl" mode="aspectFill" @tap="retakePhoto" />

        <view class="rescan-hint" @tap="retakePhoto">
          <text class="rescan-text">点击重新拍照</text>
        </view>

        <view v-if="recognizing" class="scan-overlay">
          <view class="scanner-ring">
            <view class="scanner-inner" />
          </view>
          <text class="scan-title">AI 正在分析</text>
          <text class="scan-sub">识别食物种类与营养成分<text class="dot-anim" /></text>
        </view>
      </view>

      <view v-if="!recognizing && results.length > 0" class="result-panel spring-up">
        <view v-for="(item, idx) in results" :key="idx" class="result-card">
          <view class="result-header">
            <view class="result-name-row">
              <text class="result-emoji">🍽️</text>
              <text class="result-name">{{ item.name }}</text>
            </view>
            <text class="result-amount">{{ item.amount }}</text>
          </view>

          <view class="cal-display">
            <text class="cal-num">{{ item.calories }}</text>
            <text class="cal-unit">千卡</text>
          </view>

          <view v-if="item.confidence" class="confidence-wrap">
            <view class="confidence-header">
              <text class="confidence-label">AI 确信度</text>
              <text class="confidence-value">{{ item.confidence }}%</text>
            </view>
            <view class="confidence-bar">
              <view class="confidence-fill" :style="{ width: item.confidence + '%' }" />
            </view>
          </view>

          <view v-if="item.nutrients" class="nutrient-pills">
            <view class="pill pill-protein">
              <text class="pill-dot protein-dot" />
              <text class="pill-label">蛋白质</text>
              <text class="pill-value">{{ item.nutrients.protein }}g</text>
            </view>
            <view class="pill pill-fat">
              <text class="pill-dot fat-dot" />
              <text class="pill-label">脂肪</text>
              <text class="pill-value">{{ item.nutrients.fat }}g</text>
            </view>
            <view class="pill pill-carb">
              <text class="pill-dot carb-dot" />
              <text class="pill-label">碳水</text>
              <text class="pill-value">{{ item.nutrients.carb }}g</text>
            </view>
          </view>
        </view>

        <view class="action-btns">
          <button class="action-btn secondary-btn" @tap="goManualFix">
            <text class="btn-icon">✏️</text>
            <text class="btn-text">手动修正</text>
          </button>
          <button class="action-btn primary-btn" @tap="addAllToDiet">
            <text class="btn-icon">✅</text>
            <text class="btn-text">加入记录</text>
          </button>
        </view>
      </view>

      <view v-else-if="!recognizing && recognizeFailed" class="error-panel spring-up">
        <view class="error-card">
          <text class="error-emoji">🤔</text>
          <text class="error-title">没看清楚</text>
          <text class="error-desc">请确保光线充足，食物在画面中央，再试一次吧</text>
          <view class="error-btns">
            <button class="action-btn primary-btn full-btn" @tap="retakePhoto">
              <text class="btn-icon">📸</text>
              <text class="btn-text">重新拍照</text>
            </button>
            <button class="action-btn secondary-btn full-btn" @tap="goManualFix">
              <text class="btn-icon">🔍</text>
              <text class="btn-text">手动搜索</text>
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { aiApi } from '../../api'

const imageUrl = ref('')
const recognizing = ref(false)
const recognizeFailed = ref(false)
const textInput = ref('')
const textLoading = ref(false)
const results = ref<any[]>([])
const remainFreeTimes = ref(3)
const isVip = ref(false)

const initRewardedAd = () => { /* placeholder */ }
const loadFreeTimes = async () => { /* placeholder */ }

useDidShow(() => {
  if (!Taro.getStorageSync('token')) {
    Taro.redirectTo({ url: '/pages/login/index' })
    return
  }
  if (Taro.getStorageSync('privacy_agreed')) loadFreeTimes()
  initRewardedAd()
})

const takePhoto = async () => {
  try {
    const res = await Taro.chooseImage({
      count: 1,
      sourceType: ['camera', 'album'],
      sizeType: ['compressed'],
    })
    imageUrl.value = res.tempFilePaths[0]
    recognizeFailed.value = false
    if (!isVip.value) remainFreeTimes.value = Math.max(0, remainFreeTimes.value - 1)
    recognize(res.tempFilePaths[0])
  } catch (e) { /* user cancelled */ }
}

const retakePhoto = () => {
  imageUrl.value = ''
  results.value = []
  recognizeFailed.value = false
}

const recognize = async (filePath: string) => {
  recognizing.value = true
  results.value = []
  recognizeFailed.value = false
  try {
    const res = await aiApi.recognizeFood(filePath)
    const vo = res.data?.data || res.data
    if (vo && (vo.name || vo.calories)) {
      results.value = [{
        name: vo.name || '识别食物',
        calories: vo.calories || 0,
        confidence: Math.round((vo.confidence || 0.95) * 100),
        amount: vo.amount || '1份',
        nutrients: vo.nutrients || {
          protein: vo.protein || 12,
          fat: vo.fat || 5,
          carb: vo.carbs || 30
        }
      }]
    } else {
      recognizeFailed.value = true
    }
  } catch (e: any) {
    recognizeFailed.value = true
    Taro.showToast({ title: e.message || '识别失败', icon: 'none', duration: 2000 })
  } finally {
    recognizing.value = false
  }
}

const parseText = async () => {
  if (!textInput.value.trim() || textLoading.value) return
  textLoading.value = true
  results.value = []
  recognizeFailed.value = false
  try {
    const res = await aiApi.parse(textInput.value.trim())
    const vo = res.data
    if (vo?.foods?.length) {
      results.value = vo.foods.map((f: any) => ({
        name: f.name || '识别食物',
        calories: f.calories || 0,
        confidence: Math.round((f.confidence || 0.9) * 100),
        amount: f.amount || '1份',
        nutrients: f.nutrients || { protein: f.protein || 0, fat: f.fat || 0, carb: f.carbs || 0 }
      }))
    } else if (vo?.totalCalories) {
      results.value = [{
        name: textInput.value.trim(),
        calories: vo.totalCalories,
        confidence: 85,
        amount: '1份',
        nutrients: { protein: vo.protein || 0, fat: vo.fat || 0, carb: vo.carbs || 0 }
      }]
    } else {
      recognizeFailed.value = true
    }
  } catch (e: any) {
    recognizeFailed.value = true
    Taro.showToast({ title: e.message || '解析失败', icon: 'none' })
  } finally {
    textLoading.value = false
  }
}

const addAllToDiet = async () => {
  if (!results.value.length) return
  try {
    Taro.showToast({ title: '已加入记录', icon: 'success', duration: 1500 })
    setTimeout(() => {
      imageUrl.value = ''
      results.value = []
      textInput.value = ''
    }, 1500)
  } catch (e: any) {
    Taro.showToast({ title: e.message || '添加失败', icon: 'none' })
  }
}

const goManualFix = () => Taro.switchTab({ url: '/pages/food/index' })
</script>

<style lang="scss">
$bg: linear-gradient(160deg, #1A1A2E 0%, #16213E 50%, #0F3460 100%);
$primary: #00D4AA;
$primary-gradient: linear-gradient(135deg, #00D4AA 0%, #00E8C6 100%);
$card-bg: rgba(255, 255, 255, 0.06);
$card-border: rgba(255, 255, 255, 0.08);
$text-main: #FFFFFF;
$text-sub: #8E8E93;
$text-dim: rgba(255, 255, 255, 0.4);
$radius: 28rpx;

.camera-page {
  min-height: 100vh;
  background: $bg;
  padding: 32rpx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
  box-sizing: border-box;
  color: $text-main;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.spring-up {
  animation: springUp 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

@keyframes springUp {
  0% { transform: translateY(80rpx); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

.times-badge {
  text-align: center;
  padding-top: 16rpx;

  .badge-text {
    display: inline-block;
    font-size: 24rpx;
    color: $text-sub;
    padding: 10rpx 28rpx;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 32rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.06);
  }

  .highlight {
    color: $primary;
    font-weight: 700;
    margin: 0 6rpx;
  }

  &.is-vip .badge-text {
    color: #FFD700;
    background: rgba(255, 215, 0, 0.08);
    border-color: rgba(255, 215, 0, 0.15);
  }
}

.viewfinder-wrap {
  position: relative;
  width: 480rpx;
  height: 480rpx;
  margin: 80rpx auto 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2rpx solid rgba(0, 212, 170, 0.15);

  &.pulse-1 {
    animation: pulse 2.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }

  &.pulse-2 {
    animation: pulse 2.5s cubic-bezier(0.4, 0, 0.6, 1) infinite 0.8s;
  }
}

@keyframes pulse {
  0% { transform: scale(0.85); opacity: 0.6; }
  100% { transform: scale(1.5); opacity: 0; }
}

.camera-circle {
  position: relative;
  z-index: 2;
  width: 380rpx;
  height: 380rpx;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(0, 212, 170, 0.15), rgba(0, 212, 170, 0.03));
  border: 4rpx solid rgba(0, 212, 170, 0.6);
  box-shadow:
    0 0 60rpx rgba(0, 212, 170, 0.15),
    inset 0 0 40rpx rgba(0, 212, 170, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }

  .circle-icon {
    font-size: 72rpx;
    margin-bottom: 16rpx;
  }

  .circle-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
    letter-spacing: 4rpx;
  }

  .circle-sub {
    font-size: 24rpx;
    color: $primary;
    margin-top: 12rpx;
    opacity: 0.8;
  }
}

.tips-row {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-bottom: 40rpx;

  .tip-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .tip-icon {
    font-size: 24rpx;
  }

  .tip-text {
    font-size: 22rpx;
    color: $text-dim;
  }
}

.divider {
  display: flex;
  align-items: center;
  margin: 0 20rpx 32rpx;

  .divider-line {
    flex: 1;
    height: 1rpx;
    background: rgba(255, 255, 255, 0.08);
  }

  .divider-text {
    font-size: 22rpx;
    color: $text-dim;
    margin: 0 24rpx;
    letter-spacing: 4rpx;
  }
}

.text-input-card {
  background: $card-bg;
  backdrop-filter: blur(16px);
  border: 1rpx solid $card-border;
  border-radius: $radius;
  padding: 32rpx;

  .input-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-main;
    display: block;
    margin-bottom: 20rpx;
  }

  .text-area {
    width: 100%;
    height: 160rpx;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 20rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #fff;
    box-sizing: border-box;
    border: 2rpx solid transparent;
    transition: border-color 0.3s;

    &:focus {
      border-color: $primary;
    }
  }

  .ph-color {
    color: rgba(255, 255, 255, 0.25);
  }

  .text-submit-btn {
    margin-top: 24rpx;
    width: 100%;
    height: 88rpx;
    background: $primary-gradient;
    color: #1A1A2E;
    font-size: 30rpx;
    font-weight: 700;
    border-radius: 44rpx;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(0, 212, 170, 0.3);

    &::after { border: none; }
    &:active { transform: scale(0.97); opacity: 0.9; }
    &[disabled] { opacity: 0.4; transform: none; }
  }
}

.camera-result {
  animation: fadeIn 0.3s ease-out;
}

.preview-container {
  position: relative;
  width: 100%;
  height: 520rpx;
  border-radius: $radius;
  overflow: hidden;
  margin-bottom: 32rpx;
  background: #000;

  .preview-img {
    width: 100%;
    height: 100%;
    opacity: 0.85;
  }

  .rescan-hint {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    padding: 8rpx 20rpx;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 20rpx;
    z-index: 5;

    .rescan-text {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.7);
    }
  }

  .corner {
    position: absolute;
    width: 48rpx;
    height: 48rpx;
    border: 5rpx solid $primary;
    z-index: 10;
    opacity: 0.9;
  }

  .corner-tl {
    top: 24rpx;
    left: 24rpx;
    border-right: none;
    border-bottom: none;
    border-top-left-radius: 12rpx;
  }

  .corner-tr {
    top: 24rpx;
    right: 24rpx;
    border-left: none;
    border-bottom: none;
    border-top-right-radius: 12rpx;
  }

  .corner-bl {
    bottom: 24rpx;
    left: 24rpx;
    border-right: none;
    border-top: none;
    border-bottom-left-radius: 12rpx;
  }

  .corner-br {
    bottom: 24rpx;
    right: 24rpx;
    border-left: none;
    border-top: none;
    border-bottom-right-radius: 12rpx;
  }

  .scan-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(26, 26, 46, 0.75);
    backdrop-filter: blur(8px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    z-index: 20;
    animation: fadeIn 0.3s ease-out;

    .scanner-ring {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      border: 5rpx solid rgba(0, 212, 170, 0.15);
      border-top-color: $primary;
      animation: spin 1s linear infinite;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 32rpx;

      .scanner-inner {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        border: 4rpx solid transparent;
        border-top-color: rgba(0, 212, 170, 0.5);
        animation: spin 1.5s linear infinite reverse;
      }
    }

    .scan-title {
      font-size: 34rpx;
      font-weight: 700;
      color: $primary;
      margin-bottom: 12rpx;
    }

    .scan-sub {
      font-size: 26rpx;
      color: $text-sub;
    }

    .dot-anim::after {
      content: '';
      animation: dots 1.5s steps(4, end) infinite;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes dots {
  0% { content: ''; }
  25% { content: '.'; }
  50% { content: '..'; }
  75% { content: '...'; }
  100% { content: ''; }
}

.result-panel {
  margin-top: -8rpx;
}

.result-card {
  background: $card-bg;
  backdrop-filter: blur(16px);
  border: 1rpx solid $card-border;
  border-radius: $radius;
  padding: 36rpx;
  margin-bottom: 24rpx;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .result-name-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .result-emoji {
    font-size: 36rpx;
  }

  .result-name {
    font-size: 40rpx;
    font-weight: 700;
    color: #fff;
  }

  .result-amount {
    font-size: 26rpx;
    color: $text-sub;
    background: rgba(255, 255, 255, 0.06);
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
  }
}

.cal-display {
  display: flex;
  align-items: baseline;
  margin: 16rpx 0 28rpx;

  .cal-num {
    font-size: 96rpx;
    font-weight: 900;
    line-height: 1;
    background: $primary-gradient;
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-family: 'DIN Alternate', 'SF Pro Display', -apple-system, sans-serif;
  }

  .cal-unit {
    font-size: 28rpx;
    color: $text-sub;
    margin-left: 12rpx;
    font-weight: 500;
  }
}

.confidence-wrap {
  margin-bottom: 28rpx;

  .confidence-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;

    .confidence-label {
      font-size: 24rpx;
      color: $text-sub;
    }

    .confidence-value {
      font-size: 24rpx;
      color: $primary;
      font-weight: 700;
      font-family: 'DIN Alternate', monospace;
    }
  }

  .confidence-bar {
    width: 100%;
    height: 10rpx;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 5rpx;
    overflow: hidden;

    .confidence-fill {
      height: 100%;
      background: $primary-gradient;
      border-radius: 5rpx;
      transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
    }
  }
}

.nutrient-pills {
  display: flex;
  gap: 16rpx;

  .pill {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    padding: 20rpx 12rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.04);
    border: 1rpx solid rgba(255, 255, 255, 0.06);
  }

  .pill-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
  }

  .protein-dot { background: #FF9F0A; }
  .fat-dot { background: #FF453A; }
  .carb-dot { background: #0A84FF; }

  .pill-label {
    font-size: 22rpx;
    color: $text-sub;
  }

  .pill-value {
    font-size: 28rpx;
    font-weight: 700;
    color: #fff;
    font-family: 'DIN Alternate', monospace;
  }
}

.action-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 32rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  transition: transform 0.15s ease;

  &::after { border: none; }
  &:active { transform: scale(0.96); }

  .btn-icon {
    font-size: 32rpx;
  }

  .btn-text {
    font-size: 30rpx;
    font-weight: 600;
  }
}

.primary-btn {
  background: $primary-gradient;
  color: #1A1A2E;
  box-shadow: 0 8rpx 24rpx rgba(0, 212, 170, 0.3);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 2rpx solid rgba(255, 255, 255, 0.15) !important;
}

.full-btn {
  width: 100%;
  flex: none;
}

.error-panel {
  margin-top: 32rpx;
}

.error-card {
  background: $card-bg;
  backdrop-filter: blur(16px);
  border: 1rpx solid $card-border;
  border-radius: $radius;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  .error-emoji {
    font-size: 96rpx;
    margin-bottom: 24rpx;
  }

  .error-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 12rpx;
  }

  .error-desc {
    font-size: 26rpx;
    color: $text-sub;
    line-height: 1.6;
    margin-bottom: 48rpx;
    padding: 0 20rpx;
  }

  .error-btns {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
}
</style>

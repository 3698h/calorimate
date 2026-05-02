<template>
  <view class="vip-page">
    <PrivacyPopup @agreed="onPrivacyAgreed" />
    <view class="vip-banner">
      <text class="banner-crown">👑</text>
      <text class="banner-title">食光机会员</text>
      <text class="banner-sub">解锁全部智能功能</text>
    </view>

    <view class="benefits card">
      <text class="benefits-title">会员特权</text>
      <view class="benefit-row">
        <text class="bi-icon">🤖</text>
        <view class="bi-info">
          <text class="bi-name">无限 AI 识别</text>
          <text class="bi-desc">拍照识别 · 文字解析不限次数</text>
        </view>
      </view>
      <view class="benefit-row">
        <text class="bi-icon">📊</text>
        <view class="bi-info">
          <text class="bi-name">深度营养分析</text>
          <text class="bi-desc">详细营养素报告与健康建议</text>
        </view>
      </view>
      <view class="benefit-row">
        <text class="bi-icon">📅</text>
        <view class="bi-info">
          <text class="bi-name">长期趋势追踪</text>
          <text class="bi-desc">90 天饮食趋势与体重变化</text>
        </view>
      </view>
      <view class="benefit-row">
        <text class="bi-icon">🎯</text>
        <view class="bi-info">
          <text class="bi-name">个性化方案</text>
          <text class="bi-desc">AI 定制专属饮食运动计划</text>
        </view>
      </view>
    </view>

    <view class="plans-area">
      <text class="section-title">选择套餐</text>
      <view
        v-for="plan in plans"
        :key="plan.id"
        :class="['plan-card', 'card', { selected: selected === plan.id }]"
        @tap="selected = plan.id"
      >
        <view class="plan-top">
          <text class="plan-name">{{ plan.name }}</text>
          <text class="plan-tag" v-if="plan.tag">{{ plan.tag }}</text>
        </view>
        <view class="plan-price-row">
          <text class="price-sym">¥</text>
          <text class="price-num">{{ plan.price }}</text>
          <text class="price-unit">/{{ plan.unit }}</text>
          <text class="price-old" v-if="plan.original">¥{{ plan.original }}</text>
        </view>
        <text class="plan-hint">{{ plan.hint }}</text>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="pay-btn" :loading="paying" @tap="handlePay">
        立即订阅 ¥{{ selectedPlan?.price || '--' }}
      </button>
      <button
        v-if="isDev"
        class="debug-btn"
        @tap="handleMockPaySuccess"
      >
        调试：模拟支付成功
      </button>
      <text class="pay-agree">开通即表示同意《用户协议》和《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Taro from '@tarojs/taro'
import PrivacyPopup from '../../components/PrivacyPopup.vue'
import { payApi } from '../../api'

const paying = ref(false)
const selected = ref('yearly')
const onPrivacyAgreed = () => {}

const isDev = process.env.NODE_ENV === 'development'

const plans = [
  { id: 'monthly', name: '包月会员', price: '9.9', original: '18', unit: '月', tag: '', hint: '适合短期体验' },
  { id: 'yearly', name: '年度会员', price: '99', original: '216', unit: '年', tag: '超值', hint: '每月仅 ¥8.25，省 54%' },
]

const selectedPlan = computed(() => plans.find(p => p.id === selected.value))

const handlePay = async () => {
  if (paying.value) return
  paying.value = true
  try {
    const { code } = await Taro.login()
    const res = await payApi.prepay(selected.value, code)
    const p = res.data
    await new Promise<void>((resolve, reject) => {
      Taro.requestPayment({
        appId: p.appId,
        timeStamp: p.timeStamp,
        nonceStr: p.nonceStr,
        package: p.package,
        signType: p.signType as 'MD5' | 'RSA' | 'HMAC-SHA256',
        paySign: p.paySign,
        success: () => resolve(),
        fail: (err) => reject(new Error(err.errMsg || '支付取消')),
      } as any)
    })
    Taro.showModal({
      title: '开通成功',
      content: '恭喜成为食光机会员！',
      showCancel: false,
      success: () => Taro.navigateBack(),
    })
  } catch (e: any) {
    Taro.showToast({ title: e.message || '支付失败', icon: 'none', duration: 2000 })
  } finally {
    paying.value = false
  }
}

const handleMockPaySuccess = async () => {
  try {
    await payApi.mockPaySuccess()
    Taro.showModal({
      title: '模拟支付成功',
      content: '会员已延期30天',
      showCancel: false,
    })
  } catch (e: any) {
    Taro.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  }
}
</script>

<style lang="scss">
.vip-page {
  min-height: 100vh;
  padding-bottom: 220px;
  background: #F8FBF8;
}

.vip-banner {
  background: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
  padding: 80px 40px 60px;
  text-align: center;
}

.banner-crown {
  display: block;
  font-size: 80px;
  margin-bottom: 12px;
}

.banner-title {
  display: block;
  font-size: 44px;
  font-weight: 700;
  color: #FFD700;
}

.banner-sub {
  display: block;
  font-size: 26px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8px;
}

.benefits {
  margin: -36px 24px 0;
  position: relative;
  z-index: 1;
  border-radius: 20rpx;
}

.benefits-title {
  display: block;
  font-size: 30px;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 16px;
}

.benefit-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  gap: 16px;

  & + .benefit-row {
    border-top: 1px solid #f0f0f0;
  }
}

.bi-icon {
  font-size: 44px;
}

.bi-name {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: #2C3E50;
}

.bi-desc {
  display: block;
  font-size: 22px;
  color: #7F8C8D;
  margin-top: 4px;
}

.plans-area {
  padding: 32px 24px 0;
}

.plan-card {
  border: 2px solid transparent;
  transition: border-color 0.2s;
  border-radius: 20rpx;
}

.plan-card.selected {
  border-color: #2ECC71;
  background: #F0FBF4;
}

.plan-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.plan-name {
  font-size: 30px;
  font-weight: 600;
  color: #2C3E50;
}

.plan-tag {
  background: #F39C12;
  color: #fff;
  font-size: 20px;
  padding: 4px 12px;
  border-radius: 10px;
}

.plan-price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.price-sym {
  font-size: 26px;
  color: #E74C3C;
  font-weight: 600;
}

.price-num {
  font-size: 56px;
  font-weight: 800;
  color: #E74C3C;
}

.price-unit {
  font-size: 24px;
  color: #7F8C8D;
}

.price-old {
  font-size: 24px;
  color: #BDC3C7;
  text-decoration: line-through;
  margin-left: 12px;
}

.plan-hint {
  font-size: 24px;
  color: #7F8C8D;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 24px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.pay-btn {
  width: 100%;
  height: 96px;
  line-height: 96px;
  text-align: center;
  background: linear-gradient(135deg, #F39C12 0%, #E67E22 100%);
  color: #fff;
  font-size: 34px;
  font-weight: 700;
  border-radius: 48px;
  border: none;

  &::after { border: none; }
}

.debug-btn {
  width: 100%;
  height: 72px;
  line-height: 72px;
  text-align: center;
  background: #95a5a6;
  color: #fff;
  font-size: 28px;
  border-radius: 36px;
  border: none;
  margin-top: 16px;

  &::after { border: none; }
}

.pay-agree {
  display: block;
  text-align: center;
  font-size: 20px;
  color: #BDC3C7;
  margin-top: 12px;
}
</style>

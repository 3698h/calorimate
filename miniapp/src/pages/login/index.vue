<template>
  <view class="login-page page-fade-in">
    <view class="login-bg">
      <view class="glow glow-1" />
      <view class="glow glow-2" />
      <view class="glow glow-3" />
    </view>
    <view class="login-content">
      <view class="logo-area">
        <text class="logo-emoji">🥗</text>
        <text class="app-name">食光机</text>
        <text class="app-slogan">AI智能饮食</text>
      </view>

      <view class="login-card">
        <button class="wx-login-btn" :loading="loading" @tap="handleWxLogin">
          <text>微信一键登录</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Taro from '@tarojs/taro'
import { authApi } from '../../api'

const loading = ref(false)

onMounted(() => {
  const token = Taro.getStorageSync('token')
  if (token) {
    Taro.switchTab({ url: '/pages/index/index' })
  }
})

const handleWxLogin = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const { code } = await Taro.login()
    const res = await authApi.login(code)
    Taro.setStorageSync('token', res.data.token)
    Taro.switchTab({ url: '/pages/index/index' })
  } catch (e: any) {
    Taro.showToast({ title: e.message || '登录失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(160deg, #F8FBF8 0%, #E8F5E9 100%);
}

.glow {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  filter: blur(80px);
}

.glow-1 {
  width: 400px;
  height: 400px;
  background: #2ECC71;
  top: -100px;
  right: -100px;
}

.glow-2 {
  width: 300px;
  height: 300px;
  background: #F39C12;
  bottom: 200px;
  left: -80px;
}

.glow-3 {
  width: 250px;
  height: 250px;
  background: #27AE60;
  bottom: -50px;
  right: 100px;
}

.login-content {
  position: relative;
  z-index: 1;
  padding: 0 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 280px;

  .logo-emoji {
    font-size: 120px;
    margin-bottom: 32px;
  }

  .app-name {
    font-size: 56px;
    font-weight: 800;
    color: #2C3E50;
    letter-spacing: 8px;
  }

  .app-slogan {
    font-size: 28px;
    color: #7F8C8D;
    margin-top: 16px;
    letter-spacing: 4px;
  }
}

.login-card {
  margin-top: auto;
  margin-bottom: 120px;
  width: 100%;
  background: #fff;
  border-radius: 20rpx;
  padding: 48px 40px;
  border: none;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;

  .wx-login-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100px;
    background: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
    color: #fff;
    font-size: 34px;
    font-weight: 600;
    border-radius: 50px;
    border: none;
    box-shadow: 0 8px 24px rgba(46, 204, 113, 0.4);
    transition: transform 0.2s ease;

    &::after {
      border: none;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}
</style>

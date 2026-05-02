<template>
  <view v-if="visible" class="privacy-mask">
    <view class="privacy-dialog">
      <view class="privacy-title">隐私保护提示</view>
      <scroll-view scroll-y class="privacy-body">
        <text class="privacy-text">
          感谢您使用「食光机」。在使用前，请您仔细阅读并同意《用户协议》和《隐私政策》。我们承诺严格保护您的个人信息安全。
        </text>
        <view class="privacy-links">
          <text class="privacy-link" @tap="openAgreement">《用户协议》</text>
          <text class="privacy-link" @tap="openPrivacy">《隐私政策》</text>
        </view>
      </scroll-view>
      <view class="privacy-footer">
        <button class="privacy-btn disagree" @tap="disagree">不同意</button>
        <button class="privacy-btn agree" @tap="agree">同意并继续</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Taro from '@tarojs/taro'

const emit = defineEmits<{ (e: 'agreed'): void }>()

const visible = ref(false)

onMounted(() => {
  const agreed = Taro.getStorageSync('privacy_agreed')
  if (agreed) {
    emit('agreed')
  } else {
    visible.value = true
  }
})

const agree = () => {
  Taro.setStorageSync('privacy_agreed', true)
  visible.value = false
  emit('agreed')
}

const disagree = () => {
  Taro.showModal({
    title: '提示',
    content: '您需要同意隐私政策才能使用本应用',
    showCancel: false,
  })
}

const openAgreement = () => {
  Taro.showModal({ title: '用户协议', content: '用户协议内容（待补充）', showCancel: false })
}

const openPrivacy = () => {
  Taro.showModal({ title: '隐私政策', content: '隐私政策内容（待补充）', showCancel: false })
}
</script>

<style lang="scss">
.privacy-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.privacy-dialog {
  width: 600px;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
}

.privacy-title {
  text-align: center;
  font-size: 34px;
  font-weight: 700;
  padding: 40px 32px 16px;
}

.privacy-body {
  max-height: 400px;
  padding: 0 32px 32px;
}

.privacy-text {
  font-size: 28px;
  color: #555;
  line-height: 1.8;
}

.privacy-links {
  margin-top: 16px;
}

.privacy-link {
  font-size: 28px;
  color: #2ECC71;
  margin-right: 8px;
}

.privacy-footer {
  display: flex;
  border-top: 1px solid #eee;

  .privacy-btn {
    flex: 1;
    text-align: center;
    font-size: 30px;
    padding: 28px 0;
    background: none;
    border: none;
    line-height: 1;

    &::after { border: none; }

    &.disagree {
      color: #999;
      border-right: 1px solid #eee;
    }
    &.agree {
      color: #2ECC71;
      font-weight: 600;
    }
  }
}
</style>

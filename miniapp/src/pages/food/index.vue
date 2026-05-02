<template>
  <view class="food-page">
    <view class="search-header">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索食物或拍照识别"
          placeholder-class="ph-color"
          confirm-type="search"
          @input="onInput"
          @confirm="doSearch"
        />
        <view v-if="keyword" class="search-clear" @tap="clearKeyword">
          <text class="clear-icon">✕</text>
        </view>
        <view class="search-divider" />
        <view class="search-camera" @tap="goCamera">
          <text class="camera-icon">📸</text>
        </view>
      </view>
    </view>

    <view v-if="!keyword && !searched" class="default-view fade-in">
      <view class="section-block" v-if="history.length > 0">
        <view class="section-head">
          <text class="section-title">搜索历史</text>
          <text class="section-clear" @tap="clearHistory">清除</text>
        </view>
        <view class="tag-grid">
          <view
            v-for="(item, index) in history"
            :key="index"
            class="tag-chip"
            hover-class="tag-hover"
            @tap="tapHistory(item)"
          >
            <text class="tag-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <view class="section-block">
        <view class="section-head">
          <text class="section-title">热门分类</text>
        </view>
        <view class="category-grid">
          <view
            v-for="cat in categories"
            :key="cat"
            class="category-card"
            hover-class="tag-hover"
            @tap="tapCategory(cat)"
          >
            <text class="category-icon">{{ getCategoryIcon(cat) }}</text>
            <text class="category-label">{{ cat }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="loading" class="state-wrap fade-in">
      <view class="loading-spinner" />
      <text class="state-text">正在搜索...</text>
    </view>

    <view v-else-if="error" class="state-wrap fade-in">
      <text class="state-emoji">😕</text>
      <text class="state-text">{{ error }}</text>
      <button class="btn-retry" hover-class="btn-hover" @tap="doSearch">重试</button>
    </view>

    <view v-else-if="searched && results.length === 0" class="empty-state fade-in">
      <text class="empty-emoji bounce-in">🥗</text>
      <text class="empty-title">没有找到「{{ keyword }}」</text>
      <text class="empty-desc">换个关键词试试，或用AI拍照识别</text>
      <view class="empty-actions">
        <button class="btn-camera" hover-class="btn-hover" @tap="goCamera">
          <text class="btn-emoji">📸</text>
          <text class="btn-label">拍照识别</text>
        </button>
      </view>
    </view>

    <view v-else-if="results.length > 0" class="result-section fade-in">
      <view class="result-count">
        <text class="count-text">找到 <text class="count-num">{{ results.length }}</text> 个结果</text>
      </view>
      <view class="result-list">
        <view
          v-for="item in results"
          :key="item.id"
          class="food-card"
          hover-class="card-hover"
        >
          <view class="food-thumb">
            <image v-if="item.imageUrl" class="food-thumb-img" :src="item.imageUrl" mode="aspectFill"></image>
            <text v-if="!item.imageUrl" class="food-thumb-emoji">{{ getFoodEmoji(item.name, item.category) }}</text>
          </view>
          <view class="food-info">
            <text class="food-name">{{ item.name }}</text>
            <text class="food-cal-desc">每{{ item.unit || '100g' }} · {{ item.calories }}千卡</text>
          </view>
          <view class="add-btn" hover-class="add-hover" @tap="openAddPopup(item)">
            <text class="add-icon">+</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="justAdded" class="toast-bar fade-up">
      <text>✅ 已记录：{{ justAdded }}</text>
    </view>

    <view class="ad-banner-wrap" v-if="!isVip && results.length > 0">
      <ad unit-id="adunit-xxxxxxxx" type="banner"></ad>
    </view>

    <view v-if="showPopup" class="popup-mask" @tap="closePopup">
      <view class="popup-sheet" @tap.stop>
        <view class="popup-drag" />
        <view class="popup-header">
          <view class="popup-food-row">
            <view class="popup-food-icon">{{ getFoodEmoji(selectedFood?.name, selectedFood?.category) }}</view>
            <view class="popup-food-info">
              <text class="popup-food-name">{{ selectedFood?.name }}</text>
              <text class="popup-food-cal">{{ selectedFood?.calories }} 千卡 / {{ selectedFood?.unit || '100g' }}</text>
            </view>
          </view>
          <view class="popup-close" @tap="closePopup">
            <text class="close-icon">✕</text>
          </view>
        </view>

        <view class="popup-section">
          <text class="popup-label">选择餐次</text>
          <view class="meal-grid">
            <view
              v-for="m in mealOptions"
              :key="m.value"
              :class="['meal-item', { active: mealType === m.value }]"
              @tap="mealType = m.value"
            >
              <text class="meal-icon">{{ m.icon }}</text>
              <text class="meal-text">{{ m.label }}</text>
            </view>
          </view>
        </view>

        <view class="popup-section">
          <text class="popup-label">选择份数</text>
          <view class="servings-display">
            <text class="servings-num num-font">{{ servings }}</text>
            <text class="servings-unit">份</text>
          </view>
          <view class="slider-track">
            <slider
              class="custom-slider"
              :value="servings"
              :min="0.5"
              :max="10"
              :step="0.5"
              activeColor="#2ECC71"
              backgroundColor="#E5E5EA"
              block-size="24"
              @changing="onSliderChange"
              @change="onSliderChange"
            />
          </view>
          <view class="slider-labels">
            <text class="slider-label">0.5</text>
            <text class="slider-label">5</text>
            <text class="slider-label">10</text>
          </view>
        </view>

        <view class="popup-section">
          <text class="popup-label">单位</text>
          <view class="unit-grid">
            <view
              v-for="u in unitOptions"
              :key="u"
              :class="['unit-item', { active: unit === u }]"
              @tap="unit = u"
            >
              <text class="unit-text">{{ u }}</text>
            </view>
          </view>
        </view>

        <view class="popup-summary">
          <view class="summary-row">
            <text class="summary-label">预估摄入</text>
            <view class="summary-value">
              <text class="summary-num num-font">{{ Math.round((selectedFood?.calories || 0) * servings) }}</text>
              <text class="summary-unit">千卡</text>
            </view>
          </view>
        </view>

        <button
          class="popup-confirm"
          hover-class="btn-hover"
          :loading="adding"
          @tap="confirmAdd"
        >
          <text>确认记录</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { foodApi, recordApi } from '../../api'

const keyword = ref('')
const loading = ref(false)
const error = ref('')
const searched = ref(false)
const results = ref<any[]>([])
const justAdded = ref('')
const isVip = ref(false)

const history = ref<string[]>([])
const categories = ['主食', '肉类', '蔬菜', '水果', '蛋奶', '饮品', '海鲜', '豆制品', '零食']

let timer: any = null

const showPopup = ref(false)
const selectedFood = ref<any>(null)
const servings = ref(1)
const unit = ref('份')
const mealType = ref('lunch')
const adding = ref(false)

const mealOptions = [
  { label: '早餐', value: 'breakfast', icon: '🌅' },
  { label: '午餐', value: 'lunch', icon: '☀️' },
  { label: '晚餐', value: 'dinner', icon: '🌙' },
  { label: '加餐', value: 'snack', icon: '🍪' },
]
const unitOptions = ['份', 'g', '碗', '个']

const foodNameEmojiMap: Record<string, string> = {
  '米饭': '🍚', '白米饭': '🍚', '面条': '🍜', '馒头': '🍞', '包子': '🥟',
  '饺子': '🥟', '馄饨': '🥟', '油条': '🥖', '烧饼': '🫓', '粥': '🍚',
  '米粥': '🍚', '白粥': '🍚', '红薯': '🍠', '玉米': '🌽', '土豆': '🥔',
  '鸡胸肉': '🍗', '鸡腿': '🍗', '鸡肉': '🍗', '猪肉': '🥩', '牛肉': '🥩',
  '羊肉': '🥩', '排骨': '🥩', '培根': '🥓', '香肠': '🌭', '火腿': '🍖',
  '鸡蛋': '🥚', '蒸蛋': '🥚', '煎蛋': '🍳', '炒蛋': '🍳', '鸭蛋': '🥚',
  '牛奶': '🥛', '酸奶': '🥛', '豆浆': '🥛', '豆腐': '🫘', '豆腐干': '🫘',
  '虾': '🦐', '虾仁': '🦐', '大虾': '🦐', '螃蟹': '🦀', '鱼': '🐟',
  '鲈鱼': '🐟', '三文鱼': '🐟', '带鱼': '🐟', '蛤蜊': '🦪', '扇贝': '🦪',
  '苹果': '🍎', '香蕉': '🍌', '橙子': '🍊', '橘子': '🍊', '葡萄': '🍇',
  '西瓜': '🍉', '草莓': '🍓', '蓝莓': '🫐', '桃子': '🍑', '梨': '🍐',
  '芒果': '🥭', '菠萝': '🍍', '猕猴桃': '🥝', '樱桃': '🍒', '柠檬': '🍋',
  '西红柿': '🍅', '番茄': '🍅', '黄瓜': '🥒', '白菜': '🥬', '青菜': '🥬',
  '菠菜': '🥬', '西兰花': '🥦', '胡萝卜': '🥕', '茄子': '🍆', '辣椒': '🌶️',
  '洋葱': '🧅', '蘑菇': '🍄', '生菜': '🥬', '豆芽': '🌱', '南瓜': '🎃',
  '面包': '🍞', '吐司': '🍞', '三明治': '🥪', '汉堡': '🍔', '披萨': '🍕',
  '蛋糕': '🍰', '饼干': '🍪', '巧克力': '🍫', '冰淇淋': '🍦', '糖果': '🍬',
  '可乐': '🥤', '果汁': '🧃', '咖啡': '☕', '茶': '🍵', '绿茶': '🍵',
  '啤酒': '🍺', '红酒': '🍷', '汤': '🍲', '鸡汤': '🍲', '排骨汤': '🍲',
  '火锅': '🍲', '炒饭': '🍚', '炒面': '🍜', '拉面': '🍜', '拌面': '🍜',
  '红烧肉': '🥩', '糖醋排骨': '🥩', '宫保鸡丁': '🍗',
  '麻婆豆腐': '🫘', '清蒸鱼': '🐟', '番茄炒蛋': '🍳', '回锅肉': '🥩',
  '酸菜鱼': '🐟', '小龙虾': '🦞', '烤鸭': '🦆', '叉烧': '🥩',
  '寿司': '🍣', '便当': '🍱', '沙拉': '🥗', '泡面': '🍜', '方便面': '🍜',
}

const categoryEmojiMap: Record<string, string> = {
  '主食': '🍚', '肉类': '🥩', '蔬菜': '🥬', '水果': '🍎',
  '蛋奶': '🥚', '饮品': '🥤', '海鲜': '🦐', '豆制品': '🫘',
  '零食': '🍪', '早餐': '🥪',
}

const getFoodEmoji = (name?: string, category?: string): string => {
  if (name) {
    for (const [key, emoji] of Object.entries(foodNameEmojiMap)) {
      if (name.includes(key)) return emoji
    }
  }
  return category && categoryEmojiMap[category] ? categoryEmojiMap[category] : '🍽️'
}

const getEmoji = (category?: string) => {
  return category && categoryEmojiMap[category] ? categoryEmojiMap[category] : '🍽️'
}

const getCategoryIcon = (cat: string) => {
  return categoryEmojiMap[cat] || '🍽️'
}

const loadFoods = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await foodApi.search(keyword.value.trim() || '')
    results.value = res.data || []
    searched.value = true
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const stored = Taro.getStorageSync('search_history')
  if (stored) history.value = stored
  loadFoods()
})

const saveHistory = (kw: string) => {
  if (!kw.trim()) return
  let arr = history.value.filter(i => i !== kw)
  arr.unshift(kw)
  if (arr.length > 8) arr = arr.slice(0, 8)
  history.value = arr
  Taro.setStorageSync('search_history', arr)
}

const clearHistory = () => {
  history.value = []
  Taro.removeStorageSync('search_history')
}

const onInput = () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    if (keyword.value.trim()) {
      doSearch()
    } else {
      clearKeyword()
    }
  }, 400)
}

const clearKeyword = () => {
  keyword.value = ''
  results.value = []
  searched.value = false
}

const tapCategory = (cat: string) => {
  keyword.value = cat
  doSearch()
}

const tapHistory = (kw: string) => {
  keyword.value = kw
  doSearch()
}

const doSearch = async () => {
  if (!keyword.value.trim()) return
  loading.value = true
  error.value = ''
  searched.value = true
  saveHistory(keyword.value.trim())
  try {
    const res = await foodApi.search(keyword.value.trim())
    results.value = res.data || []
  } catch (e: any) {
    error.value = e.message || '搜索失败'
  } finally {
    loading.value = false
  }
}

const goCamera = () => Taro.switchTab({ url: '/pages/camera/index' })

const openAddPopup = (food: any) => {
  selectedFood.value = food
  servings.value = 1
  unit.value = food.unit || '份'
  mealType.value = 'lunch'
  showPopup.value = true
}

const closePopup = () => { showPopup.value = false }

const onSliderChange = (e: any) => {
  servings.value = Number(e.detail.value)
}

const confirmAdd = async () => {
  if (!selectedFood.value || adding.value) return
  adding.value = true
  try {
    await recordApi.add({
      foodId: selectedFood.value.id,
      mealType: mealType.value,
      servings: servings.value,
      unit: unit.value,
    })
    justAdded.value = selectedFood.value.name
    showPopup.value = false
    setTimeout(() => { justAdded.value = '' }, 3000)
    Taro.showToast({ title: '记录成功', icon: 'success' })
  } catch (e: any) {
    Taro.showToast({ title: e.message || '添加失败', icon: 'none' })
  } finally {
    adding.value = false
  }
}

useDidShow(() => {
  if (!Taro.getStorageSync('token')) {
    Taro.redirectTo({ url: '/pages/login/index' })
  }
})
</script>

<style lang="scss">
$bg: #FFFFFF;
$bg-secondary: #F8FBF8;
$primary: #2ECC71;
$primary-light: rgba(46, 204, 113, 0.08);
$primary-gradient: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
$text-main: #2C3E50;
$text-secondary: #34495E;
$text-sub: #7F8C8D;
$border: #F0F0F0;
$card-radius: 20rpx;
$shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

.food-page {
  min-height: 100vh;
  background: $bg-secondary;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  padding-bottom: calc(env(safe-area-inset-bottom) + 32rpx);
}

.num-font {
  font-family: 'DIN Alternate', 'SF Pro Display', monospace;
}

.fade-in {
  animation: fadeIn 0.35s ease-out;
}

.bounce-in {
  animation: bounceIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

.fade-up {
  animation: fadeUp 2.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounceIn {
  0% { opacity: 0; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}

@keyframes fadeUp {
  0% { opacity: 0; transform: translate(-50%, 20rpx); }
  10% { opacity: 1; transform: translate(-50%, 0); }
  85% { opacity: 1; transform: translate(-50%, 0); }
  100% { opacity: 0; transform: translate(-50%, -16rpx); }
}

.tag-hover {
  opacity: 0.65;
  transform: scale(0.97);
}

.card-hover {
  transform: scale(0.985);
  transition: transform 0.2s ease;
}

.btn-hover {
  transform: scale(0.95) !important;
  opacity: 0.85;
  transition: all 0.2s ease;
}

.add-hover {
  transform: scale(0.88) !important;
  opacity: 0.7;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  padding: 16rpx 28rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  align-items: center;
  background: $bg-secondary;
  border-radius: 40rpx;
  height: 80rpx;
  padding: 0 12rpx 0 28rpx;
  transition: box-shadow 0.3s;

  .search-icon {
    font-size: 30rpx;
    margin-right: 12rpx;
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    font-size: 28rpx;
    color: $text-main;
    background: transparent;
    height: 80rpx;
    line-height: 80rpx;
  }

  .ph-color {
    color: #B0B0B0;
    font-size: 28rpx;
  }

  .search-clear {
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .clear-icon {
      width: 32rpx;
      height: 32rpx;
      line-height: 32rpx;
      text-align: center;
      background: #D1D1D6;
      color: #fff;
      border-radius: 50%;
      font-size: 18rpx;
    }
  }

  .search-divider {
    width: 1rpx;
    height: 32rpx;
    background: #D1D1D6;
    margin: 0 16rpx;
    flex-shrink: 0;
  }

  .search-camera {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .camera-icon {
      font-size: 34rpx;
    }
  }
}

.default-view {
  padding: 32rpx 28rpx;
}

.section-block {
  margin-bottom: 40rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 700;
    color: $text-main;
    letter-spacing: 0.5rpx;
  }

  .section-clear {
    font-size: 24rpx;
    color: $text-sub;
    padding: 8rpx;
  }
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .tag-chip {
    padding: 12rpx 28rpx;
    background: #FFFFFF;
    border-radius: 32rpx;
    box-shadow: $shadow;
    transition: all 0.2s ease;

    .tag-text {
      font-size: 26rpx;
      color: $text-secondary;
      font-weight: 500;
    }
  }
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;

  .category-card {
    width: calc(25% - 15rpx);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10rpx;
    padding: 24rpx 0;
    background: #FFFFFF;
    border-radius: 20rpx;
    box-shadow: $shadow;
    transition: all 0.2s ease;

    .category-icon {
      font-size: 44rpx;
    }

    .category-label {
      font-size: 24rpx;
      color: $text-secondary;
      font-weight: 500;
    }
  }
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .state-emoji {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .state-text {
    font-size: 28rpx;
    color: $text-sub;
    margin-top: 20rpx;
    margin-bottom: 32rpx;
  }

  .btn-retry {
    background: #FFFFFF;
    color: $text-main;
    font-size: 28rpx;
    font-weight: 600;
    padding: 0 56rpx;
    height: 76rpx;
    line-height: 76rpx;
    border-radius: 38rpx;
    border: 2rpx solid #E5E5EA;
    box-shadow: $shadow;

    &::after { border: none; }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx 60rpx;

  .empty-emoji {
    font-size: 120rpx;
    margin-bottom: 28rpx;
  }

  .empty-title {
    font-size: 34rpx;
    font-weight: 700;
    color: $text-main;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: $text-sub;
    margin-bottom: 48rpx;
  }

  .empty-actions {
    display: flex;
    gap: 24rpx;
  }

  .btn-camera {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    height: 88rpx;
    padding: 0 56rpx;
    background: $primary-gradient;
    color: #fff;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: 44rpx;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(46, 204, 113, 0.3);

    &::after { border: none; }

    .btn-emoji { font-size: 32rpx; }
    .btn-label { font-size: 30rpx; }
  }
}

.result-section {
  padding: 20rpx 28rpx;
}

.result-count {
  margin-bottom: 16rpx;

  .count-text {
    font-size: 24rpx;
    color: $text-sub;
  }

  .count-num {
    color: $primary;
    font-weight: 700;
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.food-card {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  .food-thumb {
    width: 100rpx;
    height: 100rpx;
    border-radius: 16rpx;
    background: $bg-secondary;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;
    overflow: hidden;

    .food-thumb-img {
      width: 100%;
      height: 100%;
    }

    .food-thumb-emoji {
      font-size: 48rpx;
    }
  }

  .food-info {
    flex: 1;
    min-width: 0;

    .food-name {
      font-size: 30rpx;
      font-weight: 600;
      color: $text-main;
      display: block;
      margin-bottom: 8rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .food-cal-desc {
      font-size: 24rpx;
      color: $text-sub;
      display: block;
    }
  }

  .add-btn {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $primary-gradient;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 14rpx rgba(46, 204, 113, 0.35);
    flex-shrink: 0;
    transition: transform 0.2s ease;

    .add-icon {
      color: #fff;
      font-size: 38rpx;
      font-weight: 300;
      margin-top: -2rpx;
    }
  }
}

.toast-bar {
  position: fixed;
  top: 130rpx;
  left: 50%;
  background: rgba(0, 0, 0, 0.78);
  backdrop-filter: blur(8px);
  color: #fff;
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  z-index: 200;
  white-space: nowrap;
}

.ad-banner-wrap {
  margin: 16rpx 28rpx;
  border-radius: $card-radius;
  overflow: hidden;
}

.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 500;
  display: flex;
  align-items: flex-end;
}

.popup-sheet {
  width: 100%;
  background: #fff;
  border-radius: 36rpx 36rpx 0 0;
  padding: 16rpx 32rpx 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  animation: slideUp 0.35s cubic-bezier(0.32, 0.72, 0, 1) forwards;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.popup-drag {
  width: 64rpx;
  height: 8rpx;
  background: #E5E5EA;
  border-radius: 4rpx;
  margin: 8rpx auto 24rpx;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;

  .popup-food-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .popup-food-icon {
    width: 72rpx;
    height: 72rpx;
    background: $bg-secondary;
    border-radius: 22rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
  }

  .popup-food-info {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  .popup-food-name {
    font-size: 34rpx;
    font-weight: 700;
    color: $text-main;
  }

  .popup-food-cal {
    font-size: 24rpx;
    color: $text-sub;
  }

  .popup-close {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .close-icon {
      font-size: 32rpx;
      color: #C7C7CC;
      font-weight: 300;
    }
  }
}

.popup-section {
  margin-bottom: 32rpx;

  .popup-label {
    font-size: 26rpx;
    font-weight: 600;
    color: $text-sub;
    display: block;
    margin-bottom: 16rpx;
    letter-spacing: 0.5rpx;
  }
}

.meal-grid {
  display: flex;
  gap: 16rpx;

  .meal-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
    padding: 20rpx 0;
    background: $bg-secondary;
    border-radius: 20rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s ease;

    &.active {
      background: $primary-light;
      border-color: $primary;
    }

    .meal-icon { font-size: 32rpx; }
    .meal-text {
      font-size: 24rpx;
      color: $text-secondary;
      font-weight: 500;
    }

    &.active .meal-text {
      color: $primary;
      font-weight: 600;
    }
  }
}

.servings-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 20rpx;

  .servings-num {
    font-size: 72rpx;
    font-weight: 800;
    color: $text-main;
    line-height: 1;
  }

  .servings-unit {
    font-size: 28rpx;
    color: $text-sub;
    margin-left: 8rpx;
    font-weight: 500;
  }
}

.slider-track {
  padding: 0 8rpx;

  .custom-slider {
    margin: 0;
  }
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 8rpx 0;

  .slider-label {
    font-size: 22rpx;
    color: #C7C7CC;
    font-family: 'DIN Alternate', monospace;
  }
}

.unit-grid {
  display: flex;
  gap: 16rpx;

  .unit-item {
    padding: 14rpx 36rpx;
    background: $bg-secondary;
    border-radius: 32rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s ease;

    &.active {
      background: $primary-light;
      border-color: $primary;
    }

    .unit-text {
      font-size: 28rpx;
      color: $text-secondary;
      font-weight: 500;
    }

    &.active .unit-text {
      color: $primary;
      font-weight: 600;
    }
  }
}

.popup-summary {
  background: $bg-secondary;
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 32rpx;

  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .summary-label {
    font-size: 28rpx;
    color: $text-secondary;
    font-weight: 500;
  }

  .summary-value {
    display: flex;
    align-items: baseline;
    gap: 6rpx;
  }

  .summary-num {
    font-size: 44rpx;
    font-weight: 800;
    color: $text-main;
  }

  .summary-unit {
    font-size: 24rpx;
    color: $text-sub;
    font-weight: 500;
  }
}

.popup-confirm {
  width: 100%;
  height: 96rpx;
  background: $primary-gradient;
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  border-radius: 48rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(46, 204, 113, 0.3);

  &::after { border: none; }
}
</style>

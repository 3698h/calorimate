export default defineAppConfig({
  pages: [
    'pages/index/index',
    'pages/login/index',
    'pages/food/index',
    'pages/camera/index',
    'pages/profile/index',
    'pages/profile/edit',
    'pages/vip/index',
  ],
  __usePrivacyCheck__: true,
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#2ECC71',
    navigationBarTitleText: '食光机',
    navigationBarTextStyle: 'white',
    backgroundColor: '#f5f5f5',
  },
  tabBar: {
    color: '#7F8C8D',
    selectedColor: '#2ECC71',
    backgroundColor: '#FFFFFF',
    borderStyle: 'black',
    list: [
      {
        pagePath: 'pages/index/index',
        text: '首页',
        iconPath: 'assets/tab/home.png',
        selectedIconPath: 'assets/tab/home-active.png',
      },
      {
        pagePath: 'pages/food/index',
        text: '食物',
        iconPath: 'assets/tab/food.png',
        selectedIconPath: 'assets/tab/food-active.png',
      },
      {
        pagePath: 'pages/camera/index',
        text: '拍照',
        iconPath: 'assets/tab/camera.png',
        selectedIconPath: 'assets/tab/camera-active.png',
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的',
        iconPath: 'assets/tab/profile.png',
        selectedIconPath: 'assets/tab/profile-active.png',
      },
    ],
  },
  permission: {
    'scope.camera': { desc: '用于AI拍照识别食物' },
    'scope.writePhotosAlbum': { desc: '用于保存饮食记录截图' },
    'scope.userInfo': { desc: '用于获取您的微信昵称和头像' },
  },
  requiredPrivateInfos: [],
  lazyCodeLoading: 'requiredComponents',
  usingComponents: {},
})

import Taro from '@tarojs/taro'

export const getToken = (): string => {
  return Taro.getStorageSync('token') || ''
}

export const setToken = (token: string) => {
  Taro.setStorageSync('token', token)
}

export const removeToken = () => {
  Taro.removeStorageSync('token')
}

export const isLoggedIn = (): boolean => {
  return !!getToken()
}

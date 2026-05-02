import Taro from '@tarojs/taro'

const baseUrl = 'http://localhost:8081'

const getToken = (): string => Taro.getStorageSync('token') || ''

const goLogin = () => {
  Taro.removeStorageSync('token')
  Taro.redirectTo({ url: '/pages/login/index' })
}

interface ReqOpts {
  url: string
  method?: keyof Taro.request.Method
  data?: any
  header?: Record<string, string>
  timeout?: number
}

interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

const checkPrivacy = (url: string): boolean => {
  if (url === '/api/auth/login') return true
  return !!Taro.getStorageSync('privacy_agreed')
}

export function request<T = any>(opts: ReqOpts): Promise<ApiResponse<T>> {
  if (!checkPrivacy(opts.url)) {
    return Promise.reject(new Error('请先同意隐私政策'))
  }

  const header: Record<string, string> = {
    'Content-Type': 'application/json',
    ...opts.header,
  }
  const token = getToken()
  if (token) header['Authorization'] = `Bearer ${token}`

  return new Promise((resolve, reject) => {
    Taro.request({
      url: `${baseUrl}${opts.url}`,
      method: (opts.method || 'GET') as any,
      data: opts.data,
      header,
      timeout: opts.timeout || 15000,
      success(res) {
        const body = res.data as ApiResponse<T>
        if (res.statusCode === 401) {
          goLogin()
          reject(new Error('登录已过期，请重新登录'))
          return
        }
        if (body.code === 200) {
          resolve(body)
        } else {
          reject(new Error(body.msg || '请求失败'))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络连接失败，请检查网络'))
      },
    })
  })
}

export function upload<T = any>(url: string, filePath: string, name = 'file'): Promise<ApiResponse<T>> {
  if (!checkPrivacy(url)) {
    return Promise.reject(new Error('请先同意隐私政策'))
  }

  const token = getToken()
  return new Promise((resolve, reject) => {
    Taro.uploadFile({
      url: `${baseUrl}${url}`,
      filePath,
      name,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(res) {
        const body = JSON.parse(res.data) as ApiResponse<T>
        if (body.code === 200) {
          resolve(body)
        } else {
          reject(new Error(body.msg || '上传失败'))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '上传失败'))
      },
    })
  })
}

// ========== Auth API ==========

export const authApi = {
  login: (code: string) =>
    request<{ token: string }>({ url: '/api/auth/login', method: 'POST', data: { code } }),
}

// ========== User API ==========

export const userApi = {
  getProfile: () => request<any>({ url: '/api/user/info' }),
  updateProfile: (data: any) =>
    request({ url: '/api/user/profile', method: 'PUT', data }),
  getDailyTarget: () =>
    request<{ targetCalories: number; bmr: number; formula: string }>({ url: '/api/user/daily-target' }),
  getRemainFreeTimes: () =>
    request<number>({ url: '/api/user/remain-free-times' }),
  addFreeTimes: () =>
    request({ url: '/api/user/add-free-times', method: 'POST' }),
}

// ========== Food API ==========

export const foodApi = {
  search: (keyword: string) =>
    request<any[]>({ url: `/api/foods/search?keyword=${encodeURIComponent(keyword)}` }),
  list: (params?: { name?: string; category?: string; page?: number; size?: number }) => {
    const q = new URLSearchParams()
    if (params?.name) q.append('name', params.name)
    if (params?.category) q.append('category', params.category)
    if (params?.page) q.append('page', String(params.page))
    if (params?.size) q.append('size', String(params.size))
    const qs = q.toString()
    return request({ url: `/api/food/list${qs ? '?' + qs : ''}` })
  },
}

// ========== Diet Record API ==========

export const recordApi = {
  add: (data: { foodId: number; mealType: string; servings: number; unit?: string }) =>
    request({ url: '/api/records/add', method: 'POST', data }),
  list: (date?: string) =>
    request<any>({ url: `/api/diet/log${date ? '?date=' + date : ''}` }),
  daily: (date?: string) =>
    request<any>({ url: `/api/stats/daily${date ? '?date=' + date : ''}` }),
  delete: (id: number) =>
    request({ url: `/api/diet/log/${id}`, method: 'DELETE' }),
}

// ========== AI API ==========

export const aiApi = {
  recognizeFood: (filePath: string) =>
    upload<any>('/api/ai/recognize-food', filePath),
  parse: (userInput: string, mealType?: string) =>
    request<any>({ url: '/api/ai/parse', method: 'POST', data: { userInput, mealType } }),
  advice: () =>
    request<any>({ url: '/api/ai/advice', method: 'POST' }),
}

// ========== Pay API ==========

export const payApi = {
  prepay: (planType: string) =>
    request<{ timeStamp: string; nonceStr: string; package: string; signType: string; paySign: string }>({
      url: '/api/pay/prepay',
      method: 'POST',
      data: { planType },
    }),
}

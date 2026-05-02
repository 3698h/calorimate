import Taro from '@tarojs/taro'

// ⚠️ 真机调试时需要改为局域网IP，例如 http://192.168.x.x:8081（终端运行 ipconfig 查看）
// ⚠️ 上线前务必改为正式域名
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
      timeout: opts.timeout || 30000,
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
        const msg = err.errMsg || ''
        if (msg.includes('timeout')) {
          reject(new Error('请求超时，请稍后重试'))
        } else {
          reject(new Error(msg || '网络连接失败，请检查网络'))
        }
      },
    })
  })
}

export function upload<T = any>(url: string, filePath: string, name = 'file', timeout = 60000): Promise<ApiResponse<T>> {
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
      timeout,
      success(res) {
        const body = JSON.parse(res.data) as ApiResponse<T>
        if (body.code === 200) {
          resolve(body)
        } else {
          reject(new Error(body.msg || '上传失败'))
        }
      },
      fail(err) {
        const msg = err.errMsg || ''
        if (msg.includes('timeout')) {
          reject(new Error('AI 识别超时，请稍后重试'))
        } else {
          reject(new Error(msg || '上传失败，请检查网络'))
        }
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

const mockFoods: Record<string, any[]> = {
  '米饭': [{ name: '米饭', category: '主食', calories: 116, protein: 2.6, fat: 0.3, carbs: 25.6, unit: '100g' }],
  '鸡蛋': [{ name: '鸡蛋', category: '蛋奶', calories: 144, protein: 13.3, fat: 8.8, carbs: 2.8, unit: '100g' }],
  '苹果': [{ name: '苹果', category: '水果', calories: 52, protein: 0.3, fat: 0.2, carbs: 13.7, unit: '100g' }],
  '鸡胸肉': [{ name: '鸡胸肉', category: '肉类', calories: 133, protein: 31, fat: 1.2, carbs: 0, unit: '100g' }],
}

const getMockFoodResult = (keyword: string): any[] => {
  for (const [key, foods] of Object.entries(mockFoods)) {
    if (keyword.includes(key) || key.includes(keyword)) return foods
  }
  return [{ name: keyword, category: '其他', calories: 100, protein: 3, fat: 2, carbs: 15, unit: '100g' }]
}

export const foodApi = {
  search: (keyword: string) =>
    request<any[]>({ url: `/api/foods/search?keyword=${encodeURIComponent(keyword)}` }),
  searchExternal: (keyword: string) =>
    request<any[]>({ url: `/api/foods/search-external?keyword=${encodeURIComponent(keyword)}`, timeout: 5000 })
      .catch(() => ({ code: 200, msg: 'ok', data: getMockFoodResult(keyword) })),
  importFood: (data: { name: string; category?: string; calories: number; protein?: number; fat?: number; carbs?: number; unit?: string }) =>
    request<any>({ url: '/api/foods/import', method: 'POST', data }),
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
  add: (data: { foodId?: number; foodName: string; calories: number; servings: number; unit?: string; mealType: string }) =>
    request({ url: '/api/records/add', method: 'POST', data }),
  addAi: (data: { foodName: string; calories: number; protein?: number; fat?: number; carbs?: number; mealType: string }) =>
    request({ url: '/api/records/add-ai', method: 'POST', data }),
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
    request<any>({ url: '/api/ai/parse', method: 'POST', data: { userInput, mealType }, timeout: 60000 }),
  advice: () =>
    request<any>({ url: '/api/ai/advice', method: 'POST', timeout: 60000 }),
}

// ========== Pay API ==========

export const payApi = {
  prepay: (planType: string, code: string) =>
    request<{ appId: string; timeStamp: string; nonceStr: string; package: string; signType: string; paySign: string }>({
      url: '/api/pay/prepay',
      method: 'POST',
      data: { planType, code },
    }),
  mockPaySuccess: () =>
    request<string>({
      url: '/api/pay/mock-success',
      method: 'POST',
    }),
}

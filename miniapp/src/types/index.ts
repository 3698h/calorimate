export interface User {
  id: number
  username: string
  height: number
  weight: number
  age: number
  gender: string
  targetCalories: number
  role: string
  vipLevel: number
  vipExpireTime: string
}

export interface Food {
  id: number
  name: string
  category: string
  calories: number
  protein: number
  fat: number
  carbs: number
  unit: string
}

export interface DietLogItem {
  id: number
  foodId: number
  foodName: string
  mealType: string
  servings: number
  calories: number
  logDate: string
}

export interface MealGroup {
  mealType: string
  totalCalories: number
  items: DietLogItem[]
}

export interface DailyDietVO {
  date: string
  totalCalories: number
  meals: MealGroup[]
}

export interface DailyStatsVO {
  date: string
  totalCalories: number
  totalProtein: number
  totalFat: number
  totalCarbs: number
  targetCalories: number
  mealBreakdown: Record<string, number>
}

export interface AiFoodResult {
  name: string
  amount: string
  calories: number
}

export interface AiParseResult {
  foods: AiFoodResult[]
  totalCalories: number
  mealType: string
}

export interface AiAdvice {
  evaluation: string
  onTarget: string
  suggestion: string
}

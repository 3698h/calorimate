export const formatDate = (date: Date = new Date()): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export const formatMonth = (date: Date = new Date()): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

export const formatCalories = (cal: number): string => {
  if (cal >= 1000) {
    return (cal / 1000).toFixed(1) + 'k'
  }
  return String(Math.round(cal))
}

export const getMealTypeName = (type: string): string => {
  const map: Record<string, string> = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    snack: '加餐'
  }
  return map[type] || type
}

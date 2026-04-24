export const formatDateTimeDisplay = (value?: string | null): string => {
  if (!value) return '—'

  // Common backend format like 2026-04-23T16:04:46.16266
  if (value.includes('T')) {
    const compact = value.replace('T', ' ').replace('Z', '')
    if (compact.length >= 19) return compact.slice(0, 19)
    return compact
  }

  return value
}

export const taskTypeLabel = (taskType?: string | null): string => {
  if (!taskType) return '—'
  const map: Record<string, string> = {
    PROBLEM_SOLVING: '问题解决',
    REVIEW: '评审',
    KNOWLEDGE_MANAGEMENT: '知识管理',
  }
  return map[taskType] || taskType
}

/** 与后端 UserRole 枚举一致 */
export const USER_ROLE_OPTIONS = [
  { value: 'SUPER_ADMIN', label: '超级管理员' },
  { value: 'DOMAIN_STEWARD', label: '领域行管' },
  { value: 'DEPT_ADMIN', label: '部门管理员' },
  { value: 'EXPERT_USER', label: '专家用户' },
  { value: 'REGULAR_USER', label: '普通用户' },
  { value: 'VISITOR', label: '访客' },
] as const

export function roleLabel(code: string): string {
  return USER_ROLE_OPTIONS.find((o) => o.value === code)?.label ?? code
}

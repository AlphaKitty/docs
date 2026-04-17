export type AppRole =
  | 'SUPER_ADMIN'
  | 'DOMAIN_STEWARD'
  | 'DEPT_ADMIN'
  | 'EXPERT_USER'
  | 'REGULAR_USER'

export const ROLE_GROUP = {
  STEWARD: ['DOMAIN_STEWARD', 'SUPER_ADMIN'] as const,
  EXPERT: ['EXPERT_USER', 'SUPER_ADMIN'] as const,
  SUPER_ADMIN: ['SUPER_ADMIN'] as const,
  ANY_LOGGED_IN: ['SUPER_ADMIN', 'DOMAIN_STEWARD', 'DEPT_ADMIN', 'EXPERT_USER', 'REGULAR_USER'] as const,
} as const

export function hasAnyRole(currentRoles: string[], expectedRoles: readonly string[]): boolean {
  return expectedRoles.some((r) => currentRoles.includes(r))
}

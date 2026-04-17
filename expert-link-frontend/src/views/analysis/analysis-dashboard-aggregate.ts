import type { ExpertDetail, ExpertSkill } from '@/api/types/expert'
import type { ProjectDetail } from '@/api/types/project'
import type { SkillDetail } from '@/api/types/skill'
import type { ProjectExpertAssignment } from '@/api/services/project-expert.service'

export interface PaginatedLike<T> {
  content: T[]
  totalPages: number
  totalElements: number
}

export async function fetchAllPaginated<T>(
  load: (page: number, size: number) => Promise<PaginatedLike<T>>
): Promise<{ items: T[]; totalElements: number }> {
  const size = 200
  const first = await load(0, size)
  const pages = Math.max(1, first.totalPages ?? 1)
  const items = [...(first.content ?? [])]
  for (let p = 1; p < pages; p++) {
    const next = await load(p, size)
    items.push(...(next.content ?? []))
  }
  return { items, totalElements: first.totalElements ?? items.length }
}

export function demandLevelScore(level?: string | null): number {
  if (!level) return 1
  const u = level.toUpperCase()
  if (u === 'CRITICAL') return 5
  if (u === 'HIGH') return 4
  if (u === 'MEDIUM') return 3
  if (u === 'LOW') return 2
  return 1
}

function parseLocalDate(s?: string | null): Date | null {
  if (!s) return null
  const d = new Date(s)
  return Number.isNaN(d.getTime()) ? null : d
}

function daysBetween(start: Date, end: Date): number {
  const ms = end.getTime() - start.getTime()
  return Math.max(0, Math.round(ms / (24 * 3600 * 1000)))
}

export function projectDurationDays(p: ProjectDetail): number | null {
  const start = parseLocalDate(p.startDate)
  if (!start) return null
  const end = parseLocalDate(p.endDate) ?? (p.status === 'IN_PROGRESS' || p.status === 'PLANNING' ? new Date() : null)
  if (!end) return null
  return daysBetween(start, end)
}

export function projectBudgetBucket(budget?: number | null): string {
  const b = budget ?? 0
  if (b < 100_000) return '0-10万'
  if (b < 500_000) return '10万-50万'
  if (b < 1_000_000) return '50万-100万'
  return '100万以上'
}

export function projectDurationBucket(p: ProjectDetail): string {
  const d = projectDurationDays(p)
  if (d == null) return '未知'
  if (d <= 30) return '0-30天'
  if (d <= 90) return '31-90天'
  if (d <= 180) return '91-180天'
  return '180天以上'
}

export function completionBucket(pct?: number | null): string {
  if (pct == null || Number.isNaN(pct)) return '未知'
  const v = Math.min(100, Math.max(0, pct))
  if (v <= 25) return '0-25%'
  if (v <= 50) return '26-50%'
  if (v <= 75) return '51-75%'
  return '76-100%'
}

function assignmentStart(a: ProjectExpertAssignment): Date | null {
  return parseLocalDate(a.startDate ?? null)
}

function compareAssignments(a: ProjectExpertAssignment, b: ProjectExpertAssignment): number {
  const da = assignmentStart(a)?.getTime() ?? 0
  const db = assignmentStart(b)?.getTime() ?? 0
  if (da !== db) return da - db
  const ca = a.completionPercentage ?? -1
  const cb = b.completionPercentage ?? -1
  if (ca !== cb) return ca - cb
  return (a.id ?? 0) - (b.id ?? 0)
}

/** 同一 project+expert 保留「更晚开始日期；否则更高完成度；否则更大 id」的一条 */
export function dedupeAssignments(rows: ProjectExpertAssignment[]): ProjectExpertAssignment[] {
  const map = new Map<string, ProjectExpertAssignment>()
  for (const r of rows) {
    const key = `${r.projectId}-${r.expertId}`
    const prev = map.get(key)
    if (!prev || compareAssignments(r, prev) > 0) {
      map.set(key, r)
    }
  }
  return [...map.values()]
}

export function expertCompany(e: ExpertDetail): string {
  const any = e as ExpertDetail & { currentCompany?: string }
  const raw = (e.company || any.currentCompany || '').trim()
  return raw || '未填写公司'
}

export function expertAvailabilityLabel(e: ExpertDetail): string {
  const any = e as ExpertDetail & { availabilityStatus?: string }
  const s = any.availabilityStatus
  if (s && s.trim()) return s
  return e.availability === false ? 'UNAVAILABLE' : 'AVAILABLE'
}

export function expertSkillNames(e: ExpertDetail): string[] {
  const skills = e.skills as (ExpertSkill & { name?: string })[] | undefined
  if (!skills?.length) return []
  return skills
    .map((s) => (s.skillName || s.name || '').trim())
    .filter(Boolean)
}

export interface TopSkillRow {
  rank: number
  name: string
  expertCount: number
  projectCount: number
  demandLevel: number
}

function buildSkillUsage(
  skills: SkillDetail[],
  experts: ExpertDetail[],
  projects: ProjectDetail[]
): Map<number, { expertCount: number; projectCount: number }> {
  const usage = new Map<number, { expertCount: number; projectCount: number }>()
  for (const s of skills) {
    usage.set(s.id, { expertCount: 0, projectCount: 0 })
  }
  for (const e of experts) {
    const seen = new Set<number>()
    for (const sk of (e.skills || []) as Array<{ id?: number; skillId?: number }>) {
      const sid = Number(sk.skillId ?? sk.id)
      if (!Number.isFinite(sid) || seen.has(sid) || !usage.has(sid)) continue
      seen.add(sid)
      usage.get(sid)!.expertCount += 1
    }
  }
  for (const p of projects) {
    const seen = new Set<number>()
    for (const req of p.requiredSkills || []) {
      const sid = Number(req.skillId ?? req.id)
      if (!Number.isFinite(sid) || seen.has(sid) || !usage.has(sid)) continue
      seen.add(sid)
      usage.get(sid)!.projectCount += 1
    }
  }
  return usage
}

export function buildTopSkills(skills: SkillDetail[], experts: ExpertDetail[], projects: ProjectDetail[]): TopSkillRow[] {
  const usage = buildSkillUsage(skills, experts, projects)
  const sorted = [...skills].sort((a, b) => {
    const au = usage.get(a.id) || { expertCount: 0, projectCount: 0 }
    const bu = usage.get(b.id) || { expertCount: 0, projectCount: 0 }
    if (bu.projectCount !== au.projectCount) return bu.projectCount - au.projectCount
    if (bu.expertCount !== au.expertCount) return bu.expertCount - au.expertCount
    return demandLevelScore(b.demandLevel) - demandLevelScore(a.demandLevel)
  })
  return sorted.slice(0, 20).map((s, i) => {
    const u = usage.get(s.id) || { expertCount: 0, projectCount: 0 }
    return {
      rank: i + 1,
      name: s.name,
      expertCount: u.expertCount,
      projectCount: u.projectCount,
      demandLevel: demandLevelScore(s.demandLevel),
    }
  })
}

export interface RecentMatchRow {
  projectName: string
  expertName: string
  scoreDisplay: string
  scoreIsPercent: boolean
  matchDate: string
  statusRaw: string
  statusLabel: string
}

export function assignmentStatusLabel(status?: string | null): string {
  const s = (status || 'UNKNOWN').toUpperCase()
  const map: Record<string, string> = {
    PENDING: '待确认',
    ACTIVE: '进行中',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CLOSED: '已关闭',
    CANCELLED: '已取消',
    REJECTED: '已拒绝',
    SUCCESS: '成功',
    FAILED: '失败',
  }
  return map[s] || status || '未知'
}

export function buildRecentMatches(
  deduped: ProjectExpertAssignment[],
  projectsById: Map<number, ProjectDetail>,
  expertsById: Map<number, ExpertDetail>
): RecentMatchRow[] {
  return deduped
    .map((a) => {
      const p = projectsById.get(a.projectId)
      const ex = a.expert ?? expertsById.get(a.expertId)
      const expertName = ex?.name || `专家 #${a.expertId}`
      const projectName = p?.name || `项目 #${a.projectId}`
      const pct = a.completionPercentage
      const hasPct = pct != null && !Number.isNaN(pct)
      return { a, projectName, expertName, p, hasPct, pct }
    })
    .sort((x, y) => {
      const da = assignmentStart(x.a)?.getTime() ?? 0
      const db = assignmentStart(y.a)?.getTime() ?? 0
      if (da !== db) return db - da
      const ua = parseLocalDate(x.p?.updatedAt ?? null)?.getTime() ?? 0
      const ub = parseLocalDate(y.p?.updatedAt ?? null)?.getTime() ?? 0
      if (ua !== ub) return ub - ua
      const ca = parseLocalDate(x.p?.createdAt ?? null)?.getTime() ?? 0
      const cb = parseLocalDate(y.p?.createdAt ?? null)?.getTime() ?? 0
      if (ca !== cb) return cb - ca
      return (y.a.id ?? 0) - (x.a.id ?? 0)
    })
    .map(({ a, projectName, expertName, p, hasPct, pct }) => ({
      projectName,
      expertName,
      scoreDisplay: hasPct ? `${Math.round(pct as number)}%` : assignmentStatusLabel(a.status),
      scoreIsPercent: !!hasPct,
      matchDate: (a.startDate || p?.updatedAt || p?.createdAt || '').slice(0, 10),
      statusRaw: (a.status || 'UNKNOWN').toUpperCase(),
      statusLabel: assignmentStatusLabel(a.status),
    }))
}

export function countMapTop(entries: [string, number][], limit: number): { name: string; value: number }[] {
  return entries
    .filter(([, v]) => v > 0)
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([name, value]) => ({ name, value }))
}

export function aggregateExpertSkillChart(experts: ExpertDetail[]): { name: string; value: number }[] {
  const acc = new Map<string, number>()
  for (const e of experts) {
    for (const n of expertSkillNames(e)) {
      acc.set(n, (acc.get(n) || 0) + 1)
    }
  }
  return countMapTop([...acc.entries()], 12)
}

export function aggregateExpertStatusChart(experts: ExpertDetail[]): { name: string; value: number }[] {
  const acc = new Map<string, number>()
  for (const e of experts) {
    const k = expertAvailabilityLabel(e)
    acc.set(k, (acc.get(k) || 0) + 1)
  }
  return countMapTop([...acc.entries()], 20)
}

export function aggregateExpertCompanyChart(experts: ExpertDetail[]): { name: string; value: number }[] {
  const acc = new Map<string, number>()
  for (const e of experts) {
    const k = expertCompany(e)
    acc.set(k, (acc.get(k) || 0) + 1)
  }
  return countMapTop([...acc.entries()], 12)
}

export function aggregateProjectStatusChart(projects: ProjectDetail[]): { name: string; value: number }[] {
  const acc = new Map<string, number>()
  for (const p of projects) {
    const k = p.status || 'UNKNOWN'
    acc.set(k, (acc.get(k) || 0) + 1)
  }
  return countMapTop([...acc.entries()], 20)
}

export function aggregateProjectBudgetChart(projects: ProjectDetail[]): { name: string; value: number }[] {
  const order = ['0-10万', '10万-50万', '50万-100万', '100万以上', '未知']
  const acc = new Map<string, number>()
  for (const k of order) acc.set(k, 0)
  for (const p of projects) {
    const b = p.budget
    const key = b == null || Number.isNaN(b) ? '未知' : projectBudgetBucket(b)
    acc.set(key, (acc.get(key) || 0) + 1)
  }
  return order.map((name) => ({ name, value: acc.get(name) || 0 }))
}

export function aggregateProjectDurationChart(projects: ProjectDetail[]): { name: string; value: number }[] {
  const order = ['0-30天', '31-90天', '91-180天', '180天以上', '未知']
  const acc = new Map<string, number>()
  for (const k of order) acc.set(k, 0)
  for (const p of projects) {
    const key = projectDurationBucket(p)
    acc.set(key, (acc.get(key) || 0) + 1)
  }
  return order.map((name) => ({ name, value: acc.get(name) || 0 }))
}

export function aggregateSkillHotChart(
  skills: SkillDetail[],
  experts: ExpertDetail[],
  projects: ProjectDetail[]
): { name: string; value: number }[] {
  const usage = buildSkillUsage(skills, experts, projects)
  const sorted = [...skills].sort((a, b) => {
    const au = usage.get(a.id) || { expertCount: 0, projectCount: 0 }
    const bu = usage.get(b.id) || { expertCount: 0, projectCount: 0 }
    return (bu.projectCount + bu.expertCount) - (au.projectCount + au.expertCount)
  })
  return sorted.slice(0, 12).map((s) => {
    const u = usage.get(s.id) || { expertCount: 0, projectCount: 0 }
    return { name: s.name, value: u.projectCount + u.expertCount }
  })
}

export function aggregateSkillDemandChart(skills: SkillDetail[]): { name: string; value: number }[] {
  const sorted = [...skills].sort((a, b) => demandLevelScore(b.demandLevel) - demandLevelScore(a.demandLevel))
  return sorted.slice(0, 12).map((s) => ({ name: s.name, value: demandLevelScore(s.demandLevel) }))
}

export function aggregateSkillDomainChart(
  skills: SkillDetail[],
  domains: ReadonlyArray<{ id: number; name: string }>
): { name: string; value: number }[] {
  const domainNames = new Map<number, string>()
  for (const d of domains) {
    domainNames.set(d.id, d.name)
  }
  const acc = new Map<string, number>()
  for (const s of skills) {
    const id = s.domain?.id
    const name = id != null ? domainNames.get(id) || s.domain?.name || `领域 #${id}` : '未关联领域'
    acc.set(name, (acc.get(name) || 0) + 1)
  }
  return countMapTop([...acc.entries()], 12)
}

export function aggregateMatchCoverageChart(
  projects: ProjectDetail[],
  deduped: ProjectExpertAssignment[]
): { name: string; value: number }[] {
  const withAssign = new Set(deduped.map((a) => a.projectId))
  let hit = 0
  for (const p of projects) {
    if (withAssign.has(p.id)) hit++
  }
  const miss = projects.length - hit
  return [
    { name: '已有分配', value: hit },
    { name: '尚无分配', value: Math.max(0, miss) },
  ]
}

export function aggregateMatchStatusChart(deduped: ProjectExpertAssignment[]): { name: string; value: number }[] {
  const acc = new Map<string, number>()
  for (const a of deduped) {
    const k = (a.status || 'UNKNOWN').toUpperCase()
    acc.set(k, (acc.get(k) || 0) + 1)
  }
  return countMapTop([...acc.entries()], 15)
}

export function aggregateMatchCompletionChart(deduped: ProjectExpertAssignment[]): { name: string; value: number }[] {
  const order = ['0-25%', '26-50%', '51-75%', '76-100%', '未知']
  const acc = new Map<string, number>()
  for (const k of order) acc.set(k, 0)
  for (const a of deduped) {
    const key = completionBucket(a.completionPercentage ?? null)
    acc.set(key, (acc.get(key) || 0) + 1)
  }
  return order.map((name) => ({ name, value: acc.get(name) || 0 }))
}

export function filterAssignmentsByTimeRange(
  rows: ProjectExpertAssignment[],
  range: 'week' | 'month' | 'quarter' | 'year' | 'custom' | string,
  custom?: string[] | null
): ProjectExpertAssignment[] {
  const end = new Date()
  const start = new Date(end)
  if (range === 'custom' && custom?.length === 2) {
    const a = parseLocalDate(custom[0])
    const b = parseLocalDate(custom[1])
    if (!a || !b) return rows
    const lo = a.getTime()
    const hi = b.getTime() + 24 * 3600 * 1000
    return rows.filter((r) => {
      const t = assignmentStart(r)?.getTime()
      return t != null && t >= lo && t < hi
    })
  }
  if (range === 'week') start.setDate(end.getDate() - 7)
  else if (range === 'month') start.setMonth(end.getMonth() - 1)
  else if (range === 'quarter') start.setMonth(end.getMonth() - 3)
  else if (range === 'year') start.setFullYear(end.getFullYear() - 1)
  else return rows
  const lo = start.getTime()
  const hi = end.getTime()
  return rows.filter((r) => {
    const t = assignmentStart(r)?.getTime()
    return t != null && t >= lo && t <= hi
  })
}

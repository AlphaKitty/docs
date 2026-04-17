/**
 * 仪表板统计数据
 */
export interface DashboardStats {
  totalExperts: number;
  activeExperts: number;
  totalProjects: number;
  ongoingProjects: number;
  totalSkills: number;
  totalDomains: number;
  monthlyRevenue: number;
  utilizationRate: number;
}

/**
 * 专家统计
 */
export interface ExpertStats {
  byStatus: {
    ACTIVE: number;
    INACTIVE: number;
    PENDING: number;
    ARCHIVED: number;
  };
  byLevel: {
    JUNIOR: number;
    MIDDLE: number;
    SENIOR: number;
    EXPERT: number;
  };
  byDomain: Array<{
    domainId: number;
    domainName: string;
    count: number;
  }>;
  bySkill: Array<{
    skillId: number;
    skillName: string;
    count: number;
  }>;
  ratingDistribution: Array<{
    rating: number;
    count: number;
  }>;
  hourlyRateDistribution: Array<{
    range: string;
    count: number;
  }>;
}

/**
 * 项目统计
 */
export interface ProjectStats {
  byStatus: {
    PLANNING: number;
    IN_PROGRESS: number;
    ON_HOLD: number;
    COMPLETED: number;
    CANCELLED: number;
  };
  byPriority: {
    LOW: number;
    MEDIUM: number;
    HIGH: number;
    CRITICAL: number;
  };
  byDomain: Array<{
    domainId: number;
    domainName: string;
    count: number;
    totalBudget: number;
  }>;
  budgetDistribution: Array<{
    range: string;
    count: number;
    totalBudget: number;
  }>;
  timeline: Array<{
    month: string;
    started: number;
    completed: number;
    revenue: number;
  }>;
}

/**
 * 技能统计
 */
export interface SkillStats {
  byCategory: {
    TECHNICAL: number;
    BUSINESS: number;
    DESIGN: number;
    MANAGEMENT: number;
    LANGUAGE: number;
    OTHER: number;
  };
  topSkills: Array<{
    skillId: number;
    skillName: string;
    expertCount: number;
    projectCount: number;
    averageProficiency: number;
  }>;
  demandTrend: Array<{
    month: string;
    skillId: number;
    skillName: string;
    demand: number;
  }>;
}

/**
 * 领域统计
 */
export interface DomainStats {
  hierarchy: Array<{
    domainId: number;
    domainName: string;
    parentId?: number;
    expertCount: number;
    projectCount: number;
    depth: number;
  }>;
  topDomains: Array<{
    domainId: number;
    domainName: string;
    expertCount: number;
    projectCount: number;
    totalBudget: number;
  }>;
  skillCoverage: Array<{
    domainId: number;
    domainName: string;
    requiredSkills: number;
    availableSkills: number;
    coverageRate: number;
  }>;
}

/**
 * 时间序列数据点
 */
export interface TimeSeriesPoint {
  timestamp: string;
  value: number;
}

/**
 * 趋势分析
 */
export interface TrendAnalysis {
  period: string;
  data: TimeSeriesPoint[];
  trend: 'up' | 'down' | 'stable';
  changePercent: number;
}
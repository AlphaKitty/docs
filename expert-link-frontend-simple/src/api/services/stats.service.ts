import { apiClient, handleApiError } from '../utils';
import {
  DashboardStats,
  ExpertStats,
  ProjectStats,
  SkillStats,
  ApiResponse,
} from '../types';

/**
 * 统计服务
 */
export class StatsService {
  /**
   * 获取仪表板统计数据
   */
  static async getDashboardStats(): Promise<DashboardStats> {
    try {
      const response = await apiClient.get<ApiResponse<DashboardStats>>('/stats/dashboard');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取仪表板统计数据失败');
    }
  }

  /**
   * 获取专家统计
   */
  static async getExpertStats(): Promise<ExpertStats> {
    try {
      const response = await apiClient.get<ApiResponse<ExpertStats>>('/stats/experts');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取专家统计失败');
    }
  }

  /**
   * 获取项目统计
   */
  static async getProjectStats(): Promise<ProjectStats> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectStats>>('/stats/projects');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取项目统计失败');
    }
  }

  /**
   * 获取技能统计
   */
  static async getSkillStats(): Promise<SkillStats> {
    try {
      const response = await apiClient.get<ApiResponse<SkillStats>>('/stats/skills');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能统计失败');
    }
  }

  /**
   * 获取趋势数据
   */
  static async getTrendData(
    type: 'experts' | 'projects' | 'skills',
    period: 'daily' | 'weekly' | 'monthly' = 'monthly',
    limit: number = 12
  ): Promise<any[]> {
    try {
      const response = await apiClient.get<ApiResponse<any[]>>(`/stats/trend/${type}`, {
        params: { period, limit }
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取趋势数据失败');
    }
  }

  /**
   * 获取地理分布数据
   */
  static async getGeographicDistribution(): Promise<Record<string, number>> {
    try {
      const response = await apiClient.get<ApiResponse<Record<string, number>>>('/stats/geographic');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取地理分布数据失败');
    }
  }

  /**
   * 获取活跃度统计
   */
  static async getActivityStats(): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>('/stats/activity');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取活跃度统计失败');
    }
  }

  /**
   * 获取系统使用统计
   */
  static async getSystemUsageStats(): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>('/stats/system-usage');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取系统使用统计失败');
    }
  }

  /**
   * 获取性能指标
   */
  static async getPerformanceMetrics(): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>('/stats/performance');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取性能指标失败');
    }
  }

  /**
   * 获取自定义报表
   */
  static async getCustomReport(params: any): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>('/stats/custom-report', {
        params
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取自定义报表失败');
    }
  }

  /**
   * 导出统计报表
   */
  static async exportStatsReport(params: any): Promise<Blob> {
    try {
      const response = await apiClient.get('/stats/export', {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '导出统计报表失败');
    }
  }
}
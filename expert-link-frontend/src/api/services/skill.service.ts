import { apiClient, handleApiError } from '../utils';
import type {
  SkillDetail,
  CreateSkillRequest,
  UpdateSkillRequest,
  SkillQueryParams,
  SkillStats,
  SkillTreeNode,
  ApiResponse,
  PaginatedResponse,
} from '../types';

/**
 * 技能服务
 */
export class SkillService {
  /**
   * 获取技能列表
   */
  static async getSkills(params?: SkillQueryParams): Promise<PaginatedResponse<SkillDetail>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<SkillDetail>>>(
        '/skills',
        { params }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能列表失败');
    }
  }

  /**
   * 获取技能详情
   */
  static async getSkillById(id: number): Promise<SkillDetail> {
    try {
      const response = await apiClient.get<ApiResponse<SkillDetail>>(`/skills/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能详情失败');
    }
  }

  /**
   * 创建技能
   */
  static async createSkill(data: CreateSkillRequest): Promise<SkillDetail> {
    try {
      const response = await apiClient.post<ApiResponse<SkillDetail>>('/skills', data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '创建技能失败');
    }
  }

  /**
   * 更新技能
   */
  static async updateSkill(id: number, data: UpdateSkillRequest): Promise<SkillDetail> {
    try {
      const response = await apiClient.put<ApiResponse<SkillDetail>>(`/skills/${id}`, data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新技能失败');
    }
  }

  /**
   * 删除技能
   */
  static async deleteSkill(id: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>(`/skills/${id}`);
    } catch (error) {
      throw handleApiError(error, '删除技能失败');
    }
  }

  /**
   * 批量删除技能
   */
  static async deleteSkills(ids: number[]): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>('/skills/batch', {
        data: { ids }
      });
    } catch (error) {
      throw handleApiError(error, '批量删除技能失败');
    }
  }

  /**
   * 获取技能统计
   */
  static async getSkillStats(): Promise<SkillStats> {
    try {
      const response = await apiClient.get<ApiResponse<SkillStats>>('/skills/stats');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能统计失败');
    }
  }

  /**
   * 获取技能树
   */
  static async getSkillTree(): Promise<SkillTreeNode[]> {
    try {
      const response = await apiClient.get<ApiResponse<SkillTreeNode[]>>('/skills/tree');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能树失败');
    }
  }

  /**
   * 搜索技能
   */
  static async searchSkills(keyword: string): Promise<SkillDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<SkillDetail[]>>('/skills/search', {
        params: { keyword }
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '搜索技能失败');
    }
  }

  /**
   * 导出技能数据
   */
  static async exportSkills(params?: SkillQueryParams): Promise<Blob> {
    try {
      const response = await apiClient.get<Blob>('/skills/export', {
        params,
        responseType: 'blob'
      });
      return response;
    } catch (error) {
      throw handleApiError(error, '导出技能数据失败');
    }
  }

  /**
   * 导入技能数据
   */
  static async importSkills(file: File): Promise<ApiResponse<void>> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      return await apiClient.post<ApiResponse<void>>('/skills/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } catch (error) {
      throw handleApiError(error, '导入技能数据失败');
    }
  }

  /**
   * 获取技能分类统计
   */
  static async getCategoryStats(): Promise<Record<string, number>> {
    try {
      const response = await apiClient.get<ApiResponse<Record<string, number>>>('/skills/categories');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取技能分类统计失败');
    }
  }

  /**
   * 获取热门技能
   */
  static async getPopularSkills(limit: number = 10): Promise<SkillDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<SkillDetail>>>('/skills/popular', {
        params: { limit }
      });
      return response.data.content;
    } catch (error) {
      throw handleApiError(error, '获取热门技能失败');
    }
  }
}
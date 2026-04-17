import { apiClient, handleApiError } from '../utils';
import {
  ExpertDetail,
  CreateExpertRequest,
  UpdateExpertRequest,
  ExpertQueryParams,
  ExpertStats,
  ApiResponse,
  PaginatedResponse,
} from '../types';

/**
 * 专家服务
 */
export class ExpertService {
  /**
   * 获取专家列表
   */
  static async getExperts(params?: ExpertQueryParams): Promise<PaginatedResponse<ExpertDetail>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ExpertDetail>>>(
        '/experts',
        { params }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取专家列表失败');
    }
  }

  /**
   * 获取专家详情
   */
  static async getExpertById(id: number): Promise<ExpertDetail> {
    try {
      const response = await apiClient.get<ApiResponse<ExpertDetail>>(`/experts/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取专家详情失败');
    }
  }

  /**
   * 创建专家
   */
  static async createExpert(data: CreateExpertRequest): Promise<ExpertDetail> {
    try {
      const response = await apiClient.post<ApiResponse<ExpertDetail>>('/experts', data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '创建专家失败');
    }
  }

  /**
   * 更新专家
   */
  static async updateExpert(id: number, data: UpdateExpertRequest): Promise<ExpertDetail> {
    try {
      const response = await apiClient.put<ApiResponse<ExpertDetail>>(`/experts/${id}`, data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新专家失败');
    }
  }

  /**
   * 删除专家
   */
  static async deleteExpert(id: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>(`/experts/${id}`);
    } catch (error) {
      throw handleApiError(error, '删除专家失败');
    }
  }

  /**
   * 批量删除专家
   */
  static async deleteExperts(ids: number[]): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>('/experts/batch', {
        data: { ids }
      });
    } catch (error) {
      throw handleApiError(error, '批量删除专家失败');
    }
  }

  /**
   * 更新专家状态
   */
  static async updateExpertStatus(id: number, status: string): Promise<ExpertDetail> {
    try {
      const response = await apiClient.patch<ApiResponse<ExpertDetail>>(
        `/experts/${id}/status`,
        { status }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新专家状态失败');
    }
  }

  /**
   * 获取专家统计
   */
  static async getExpertStats(): Promise<ExpertStats> {
    try {
      const response = await apiClient.get<ApiResponse<ExpertStats>>('/experts/stats');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取专家统计失败');
    }
  }

  /**
   * 搜索专家
   */
  static async searchExperts(keyword: string): Promise<ExpertDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<ExpertDetail[]>>('/experts/search', {
        params: { keyword }
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '搜索专家失败');
    }
  }

  /**
   * 导出专家数据
   */
  static async exportExperts(params?: ExpertQueryParams): Promise<Blob> {
    try {
      const response = await apiClient.get('/experts/export', {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '导出专家数据失败');
    }
  }

  /**
   * 导入专家数据
   */
  static async importExperts(file: File): Promise<ApiResponse<void>> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      return await apiClient.post<ApiResponse<void>>('/experts/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } catch (error) {
      throw handleApiError(error, '导入专家数据失败');
    }
  }
}
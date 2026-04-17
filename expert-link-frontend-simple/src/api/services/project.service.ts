import { apiClient, handleApiError } from '../utils';
import {
  ProjectDetail,
  CreateProjectRequest,
  UpdateProjectRequest,
  ProjectQueryParams,
  ProjectStats,
  ApiResponse,
  PaginatedResponse,
} from '../types';

/**
 * 项目服务
 */
export class ProjectService {
  /**
   * 获取项目列表
   */
  static async getProjects(params?: ProjectQueryParams): Promise<PaginatedResponse<ProjectDetail>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProjectDetail>>>(
        '/projects',
        { params }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取项目列表失败');
    }
  }

  /**
   * 获取项目详情
   */
  static async getProjectById(id: number): Promise<ProjectDetail> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectDetail>>(`/projects/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取项目详情失败');
    }
  }

  /**
   * 创建项目
   */
  static async createProject(data: CreateProjectRequest): Promise<ProjectDetail> {
    try {
      const response = await apiClient.post<ApiResponse<ProjectDetail>>('/projects', data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '创建项目失败');
    }
  }

  /**
   * 更新项目
   */
  static async updateProject(id: number, data: UpdateProjectRequest): Promise<ProjectDetail> {
    try {
      const response = await apiClient.put<ApiResponse<ProjectDetail>>(`/projects/${id}`, data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新项目失败');
    }
  }

  /**
   * 删除项目
   */
  static async deleteProject(id: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>(`/projects/${id}`);
    } catch (error) {
      throw handleApiError(error, '删除项目失败');
    }
  }

  /**
   * 批量删除项目
   */
  static async deleteProjects(ids: number[]): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>('/projects/batch', {
        data: { ids }
      });
    } catch (error) {
      throw handleApiError(error, '批量删除项目失败');
    }
  }

  /**
   * 更新项目状态
   */
  static async updateProjectStatus(id: number, status: string): Promise<ProjectDetail> {
    try {
      const response = await apiClient.patch<ApiResponse<ProjectDetail>>(
        `/projects/${id}/status`,
        { status }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新项目状态失败');
    }
  }

  /**
   * 获取项目统计
   */
  static async getProjectStats(): Promise<ProjectStats> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectStats>>('/projects/stats');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取项目统计失败');
    }
  }

  /**
   * 搜索项目
   */
  static async searchProjects(keyword: string): Promise<ProjectDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectDetail[]>>('/projects/search', {
        params: { keyword }
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '搜索项目失败');
    }
  }

  /**
   * 导出项目数据
   */
  static async exportProjects(params?: ProjectQueryParams): Promise<Blob> {
    try {
      const response = await apiClient.get('/projects/export', {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '导出项目数据失败');
    }
  }

  /**
   * 导入项目数据
   */
  static async importProjects(file: File): Promise<ApiResponse<void>> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      return await apiClient.post<ApiResponse<void>>('/projects/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } catch (error) {
      throw handleApiError(error, '导入项目数据失败');
    }
  }

  /**
   * 为项目分配专家
   */
  static async assignExpert(projectId: number, expertId: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.post<ApiResponse<void>>(`/projects/${projectId}/experts/${expertId}`);
    } catch (error) {
      throw handleApiError(error, '分配专家失败');
    }
  }

  /**
   * 从项目移除专家
   */
  static async removeExpert(projectId: number, expertId: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>(`/projects/${projectId}/experts/${expertId}`);
    } catch (error) {
      throw handleApiError(error, '移除专家失败');
    }
  }

  /**
   * 获取项目专家列表
   */
  static async getProjectExperts(projectId: number): Promise<any[]> {
    try {
      const response = await apiClient.get<ApiResponse<any[]>>(`/projects/${projectId}/experts`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取项目专家列表失败');
    }
  }
}
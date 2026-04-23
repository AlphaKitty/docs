import { apiClient, handleApiError } from '../utils';
import type {
  DomainDetail,
  CreateDomainRequest,
  UpdateDomainRequest,
  DomainQueryParams,
  ApiResponse,
  PaginatedResponse,
} from '../types';

/**
 * 领域服务
 */
export class DomainService {
  static async downloadImportTemplate(): Promise<Blob> {
    try {
      return await apiClient.get<Blob>('/domains/import-template', {
        responseType: 'blob'
      })
    } catch (error) {
      throw handleApiError(error, '下载领域导入模板失败')
    }
  }
  static async getDomainStats(id: number): Promise<{
    domainId: number
    subtreeDomainCount: number
    expertCount: number
    projectCount: number
    subtreeDomainIds: number[]
  }> {
    try {
      const response = await apiClient.get<ApiResponse<{
        domainId: number
        subtreeDomainCount: number
        expertCount: number
        projectCount: number
        subtreeDomainIds: number[]
      }>>(`/domains/${id}/stats`)
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取领域统计失败')
    }
  }

  static async getDomainStatsBatch(domainIds?: number[]): Promise<Array<{
    domainId: number
    subtreeDomainCount: number
    expertCount: number
    projectCount: number
    subtreeDomainIds: number[]
  }>> {
    try {
      const response = await apiClient.post<ApiResponse<Array<{
        domainId: number
        subtreeDomainCount: number
        expertCount: number
        projectCount: number
        subtreeDomainIds: number[]
      }>>>('/domains/stats', domainIds && domainIds.length ? { domainIds } : {})
      return response.data
    } catch (error) {
      throw handleApiError(error, '批量获取领域统计失败')
    }
  }

  static async batchAddExperts(domainId: number, ownerIds: number[]): Promise<{
    requestedCount: number
    createdCount: number
    skippedCount: number
    createdExpertIds: number[]
    skippedOwnerIds: number[]
  }> {
    try {
      const response = await apiClient.post<ApiResponse<{
        requestedCount: number
        createdCount: number
        skippedCount: number
        createdExpertIds: number[]
        skippedOwnerIds: number[]
      }>>(`/domains/${domainId}/experts/batch-add`, { ownerIds })
      return response.data
    } catch (error) {
      throw handleApiError(error, '批量纳入领域专家失败')
    }
  }

  /**
   * 获取领域列表
   */
  static async getDomains(params?: DomainQueryParams): Promise<PaginatedResponse<DomainDetail>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<DomainDetail>>>(
        '/domains',
        { params }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取领域列表失败');
    }
  }

  /**
   * 获取领域详情
   */
  static async getDomainById(id: number): Promise<DomainDetail> {
    try {
      const response = await apiClient.get<ApiResponse<DomainDetail>>(`/domains/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取领域详情失败');
    }
  }

  /**
   * 创建领域
   */
  static async createDomain(data: CreateDomainRequest): Promise<DomainDetail> {
    try {
      const response = await apiClient.post<ApiResponse<DomainDetail>>('/domains', data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '创建领域失败');
    }
  }

  /**
   * 更新领域
   */
  static async updateDomain(id: number, data: UpdateDomainRequest): Promise<DomainDetail> {
    try {
      const response = await apiClient.put<ApiResponse<DomainDetail>>(`/domains/${id}`, data);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '更新领域失败');
    }
  }

  /**
   * 删除领域
   */
  static async deleteDomain(id: number): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>(`/domains/${id}`);
    } catch (error) {
      throw handleApiError(error, '删除领域失败');
    }
  }

  /**
   * 批量删除领域
   */
  static async deleteDomains(ids: number[]): Promise<ApiResponse<void>> {
    try {
      return await apiClient.delete<ApiResponse<void>>('/domains/batch', {
        data: { ids }
      });
    } catch (error) {
      throw handleApiError(error, '批量删除领域失败');
    }
  }

  /**
   * 搜索领域
   */
  static async searchDomains(keyword: string): Promise<DomainDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<DomainDetail[]>>('/domains/search', {
        params: { keyword }
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error, '搜索领域失败');
    }
  }

  /**
   * 导出领域数据
   */
  static async exportDomains(params?: DomainQueryParams): Promise<Blob> {
    try {
      const response = await apiClient.get<Blob>('/domains/export', {
        params,
        responseType: 'blob'
      });
      return response;
    } catch (error) {
      throw handleApiError(error, '导出领域数据失败');
    }
  }

  /**
   * 导入领域数据
   */
  static async importDomains(file: File): Promise<ApiResponse<{
    total: number
    success: number
    failed: number
    skipped: number
    errors: Array<{ row: number; message: string }>
  }>> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      return await apiClient.post<ApiResponse<{
        total: number
        success: number
        failed: number
        skipped: number
        errors: Array<{ row: number; message: string }>
      }>>('/domains/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } catch (error) {
      throw handleApiError(error, '导入领域数据失败');
    }
  }

  /**
   * 获取领域层级结构
   */
  static async getDomainHierarchy(): Promise<DomainDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<DomainDetail[]>>('/domains/hierarchy');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取领域层级结构失败');
    }
  }

  /**
   * 获取子领域
   */
  static async getSubdomains(parentId: number): Promise<DomainDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<DomainDetail[]>>(`/domains/${parentId}/subdomains`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取子领域失败');
    }
  }

  /**
   * 获取顶级领域
   */
  static async getTopLevelDomains(): Promise<DomainDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<DomainDetail[]>>('/domains/top-level');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取顶级领域失败');
    }
  }

  /** 配置领域行管（SUPER_ADMIN） */
  static async replaceDomainStewards(domainId: number, userIds: number[]): Promise<DomainDetail> {
    try {
      const response = await apiClient.put<ApiResponse<DomainDetail>>(`/domains/${domainId}/stewards`, {
        userIds,
      })
      return response.data
    } catch (error) {
      throw handleApiError(error, '更新领域行管失败')
    }
  }
}
import { apiClient, handleApiError } from '../utils';
import type {
  ExpertDetail,
  CreateExpertRequest,
  UpdateExpertRequest,
  ExpertQueryParams,
  ExpertStats,
  ApiResponse,
  PaginatedResponse,
  UserPickerItem,
} from '../types';

function toExpertPayload(data: CreateExpertRequest | UpdateExpertRequest) {
  return {
    name: data.name,
    email: data.email,
    phoneNumber: data.phone,
    wechatId: data.wechat,
    currentCompany: data.company,
    currentPosition: data.position,
    biography: data.introduction,
    yearsOfExperience: data.experienceYears,
    hourlyRate: data.hourlyRate,
    availabilityStatus: data.availability === false ? 'UNAVAILABLE' : 'AVAILABLE',
    ...(data.avatar !== undefined && data.avatar !== '' ? { avatar: data.avatar } : {}),
  };
}

function toExpertParams(data: CreateExpertRequest | UpdateExpertRequest) {
  const params: Record<string, unknown> = {
    primaryDomainId: data.primaryDomainId,
    skillIds: data.skillIds?.length ? data.skillIds : undefined,
    domainIds: data.domainIds?.length ? data.domainIds : undefined,
  }
  if ('ownerId' in data && data.ownerId != null) {
    params.ownerId = data.ownerId
  }
  if ('designationId' in data && data.designationId != null) {
    params.designationId = data.designationId
  }
  return params
}

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
      const response = await apiClient.post<ApiResponse<ExpertDetail>>(
        '/experts',
        toExpertPayload(data),
        { params: toExpertParams(data) }
      )
      return response.data
    } catch (error) {
      throw handleApiError(error, '创建专家失败')
    }
  }

  /** 尚未建立专家档案的用户（管理员/部门管理员） */
  static async getUserCandidates(
    keyword: string,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<UserPickerItem>> {
    try {
      const res = await apiClient.get<ApiResponse<PaginatedResponse<UserPickerItem>>>(
        '/experts/user-candidates',
        { params: { keyword: keyword || undefined, page, size } }
      )
      return res.data
    } catch (e) {
      throw handleApiError(e, '搜索用户失败')
    }
  }

  /**
   * 更新专家
   */
  static async updateExpert(id: number, data: UpdateExpertRequest): Promise<ExpertDetail> {
    try {
      const response = await apiClient.put<ApiResponse<ExpertDetail>>(
        `/experts/${id}`,
        toExpertPayload(data),
        { params: toExpertParams(data) }
      );
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
      const response = await apiClient.put<ApiResponse<ExpertDetail>>(
        `/experts/${id}/status`,
        null,
        { params: { status } }
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
      const [totalResponse, activeResponse] = await Promise.all([
        apiClient.get<ApiResponse<PaginatedResponse<ExpertDetail>>>('/experts', {
          params: { page: 0, size: 1 },
        }),
        apiClient.get<ApiResponse<number>>('/experts/count/active'),
      ]);

      return {
        totalExperts: totalResponse.data.totalElements,
        activeExperts: activeResponse.data,
      } as unknown as ExpertStats;
    } catch (error) {
      throw handleApiError(error, '获取专家统计失败');
    }
  }

  /**
   * 搜索专家
   */
  static async searchExperts(keyword: string): Promise<PaginatedResponse<ExpertDetail>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ExpertDetail>>>('/experts/search', {
        params: { keyword, page: 0, size: 20 }
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
      return await apiClient.get<Blob>('/experts/export', {
        params,
        responseType: 'blob',
      });
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

  /** 某领域下的专家（用于调⽤申请指派等） */
  static async getExpertsByDomain(domainId: number): Promise<ExpertDetail[]> {
    try {
      const response = await apiClient.get<ApiResponse<ExpertDetail[]>>(`/experts/domain/${domainId}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取领域专家失败');
    }
  }
}
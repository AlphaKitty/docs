import { apiClient, handleApiError } from '../utils';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ChangePasswordRequest,
  ResetPasswordRequest,
  UserInfo,
  ApiResponse,
} from '../types';

/**
 * 认证服务
 */
export class AuthService {
  /**
   * 用户登录
   */
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<ApiResponse<LoginResponse>>(
        '/auth/login',
        credentials
      );
      
      // 保存token到localStorage
      if (response.data?.token) {
        localStorage.setItem('auth_token', response.data.token);
      }
      
      return response.data;
    } catch (error) {
      throw handleApiError(error, '登录失败');
    }
  }

  /**
   * 用户注册
   */
  static async register(data: RegisterRequest): Promise<ApiResponse<void>> {
    try {
      return await apiClient.post<ApiResponse<void>>('/auth/register', data);
    } catch (error) {
      throw handleApiError(error, '注册失败');
    }
  }

  /**
   * 获取当前用户信息
   */
  static async getCurrentUser(): Promise<UserInfo> {
    try {
      const response = await apiClient.get<ApiResponse<UserInfo>>('/auth/me');
      return response.data;
    } catch (error) {
      throw handleApiError(error, '获取用户信息失败');
    }
  }

  /**
   * 修改密码
   */
  static async changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    try {
      return await apiClient.put<ApiResponse<void>>('/auth/change-password', data);
    } catch (error) {
      throw handleApiError(error, '修改密码失败');
    }
  }

  /**
   * 请求重置密码
   */
  static async requestPasswordReset(email: string): Promise<ApiResponse<void>> {
    try {
      return await apiClient.post<ApiResponse<void>>('/auth/request-reset', { email });
    } catch (error) {
      throw handleApiError(error, '请求重置密码失败');
    }
  }

  /**
   * 重置密码
   */
  static async resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
    try {
      return await apiClient.post<ApiResponse<void>>('/auth/reset-password', data);
    } catch (error) {
      throw handleApiError(error, '重置密码失败');
    }
  }

  /**
   * 用户登出
   */
  static logout(): void {
    localStorage.removeItem('auth_token');
    // 可以跳转到登录页
    window.location.href = '/login';
  }

  /**
   * 检查是否已登录
   */
  static isAuthenticated(): boolean {
    return !!localStorage.getItem('auth_token');
  }

  /**
   * 获取认证token
   */
  static getToken(): string | null {
    return localStorage.getItem('auth_token');
  }
}
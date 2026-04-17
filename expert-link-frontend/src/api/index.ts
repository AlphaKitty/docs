/**
 * API模块主入口
 */

// 导出配置
export { API_CONFIG, ENV } from './config';

// 导出工具
export * from './utils';

// 导出类型
export * from './types';

// 导出服务
export * from './services';

// 创建API客户端实例
import { createAxiosInstance } from './utils/axios';
import { API_CONFIG } from './config';

export const apiClient = createAxiosInstance({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
});

// 导出API状态
export const API_STATUS = {
  READY: true,
  MOCK_MODE: API_CONFIG.USE_MOCK,
  BASE_URL: API_CONFIG.BASE_URL,
} as const;
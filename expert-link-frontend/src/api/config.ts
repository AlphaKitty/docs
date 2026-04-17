/**
 * API配置
 */

// 环境配置
export const API_CONFIG = {
  // API基础URL
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api',
  
  // 请求超时时间（毫秒）
  TIMEOUT: 30000,
  
  // 是否启用模拟数据
  USE_MOCK: import.meta.env.VITE_USE_MOCK === 'true' || false,
  
  // 模拟数据延迟（毫秒）
  MOCK_DELAY: 500,
  
  // 认证配置
  AUTH: {
    TOKEN_KEY: 'expertlink_token',
    USER_KEY: 'expertlink_user',
  },
  
  // 分页配置
  PAGINATION: {
    DEFAULT_PAGE_SIZE: 10,
    MAX_PAGE_SIZE: 100,
  },
  
  // 上传配置
  UPLOAD: {
    MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
    ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
  },
  
  // 缓存配置
  CACHE: {
    ENABLED: true,
    DEFAULT_TTL: 5 * 60 * 1000, // 5分钟
  },
  
  // 重试配置
  RETRY: {
    MAX_ATTEMPTS: 3,
    BASE_DELAY: 1000,
  },
} as const;

// 导出环境变量
export const ENV = {
  MODE: import.meta.env.MODE,
  DEV: import.meta.env.DEV,
  PROD: import.meta.env.PROD,
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  VITE_USE_MOCK: import.meta.env.VITE_USE_MOCK,
} as const;
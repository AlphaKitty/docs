/**
 * API响应基础接口
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

/**
 * 分页参数
 */
export interface PaginationParams {
  page: number;
  size: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 分页响应
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * API错误接口
 */
export interface ApiError {
  status: number;
  message: string;
  data?: any;
}

/**
 * 查询参数
 */
export interface QueryParams {
  [key: string]: any;
}

/**
 * 认证令牌
 */
export interface AuthToken {
  token: string;
  expiresIn: number;
  tokenType: string;
}
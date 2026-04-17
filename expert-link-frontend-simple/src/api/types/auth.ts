/**
 * 登录请求
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  expiresIn: number;
  tokenType: string;
  user: UserInfo;
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number;
  username: string;
  email: string;
  role: string;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 注册请求
 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  confirmPassword: string;
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * 重置密码请求
 */
export interface ResetPasswordRequest {
  email: string;
  token: string;
  newPassword: string;
}
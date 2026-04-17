import type { ApiError } from '../types/common';

/**
 * 处理API错误
 * @param error API错误对象
 * @param defaultMessage 默认错误消息
 */
export function handleApiError(error: any, defaultMessage = '操作失败'): ApiError {
  if (error?.status) {
    return {
      status: error.status,
      message: error.message || defaultMessage,
      data: error.data,
    };
  }
  
  return {
    status: -1,
    message: defaultMessage,
    data: error,
  };
}

/**
 * 显示错误提示
 * @param error 错误对象
 * @param showToast 显示提示的函数
 */
export function showError(error: ApiError, showToast?: (message: string) => void) {
  const message = error.message || '操作失败';
  
  if (showToast) {
    showToast(message);
  } else {
    console.error(`API错误 [${error.status}]: ${message}`);
  }
  
  // 如果是认证错误，可以跳转到登录页
  if (error.status === 401) {
    // 清除本地存储的token
    localStorage.removeItem('auth_token');
    // 跳转到登录页
    window.location.href = '/login';
  }
}

/**
 * 检查响应是否成功
 * @param response API响应
 */
export function isSuccess(response: any): boolean {
  return response && response.code === 200;
}

/**
 * 从响应中提取数据
 * @param response API响应
 */
export function extractData<T>(response: any): T {
  return response?.data;
}
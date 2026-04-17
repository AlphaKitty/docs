import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';

export interface TypedApiClient {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>;
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>;
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
}

function attachInterceptors(instance: AxiosInstance) {
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      if (config.data instanceof FormData) {
        delete config.headers['Content-Type'];
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (response) => response.data,
    (error) => {
      if (error.response) {
        const { status, data } = error.response;

        switch (status) {
          case 401:
            localStorage.removeItem('auth_token')
            if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
              window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
            }
            break;
          case 403:
            console.error('禁止访问');
            break;
          case 404:
            console.error('请求的资源不存在');
            break;
          case 500:
            console.error('服务器内部错误');
            break;
          default:
            console.error(`请求错误: ${status}`);
        }

        return Promise.reject({
          status,
          message: data?.message || '请求失败',
          data,
        });
      }

      if (error.request) {
        console.error('网络错误，请检查网络连接');
        return Promise.reject({
          status: 0,
          message: '网络错误，请检查网络连接',
        });
      }

      console.error('请求配置错误:', error.message);
      return Promise.reject({
        status: -1,
        message: error.message,
      });
    }
  );
}

export function createAxiosInstance(config?: AxiosRequestConfig): TypedApiClient {
  const instance = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
    ...config,
  });

  attachInterceptors(instance);
  return instance as unknown as TypedApiClient;
}

const apiClient = createAxiosInstance();

export default apiClient;
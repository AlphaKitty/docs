import axios from 'axios';

// 创建Axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8888/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          console.error('未授权，请重新登录');
          // 可以跳转到登录页
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
        data: data,
      });
    } else if (error.request) {
      // 请求已发出但没有收到响应
      console.error('网络错误，请检查网络连接');
      return Promise.reject({
        status: 0,
        message: '网络错误，请检查网络连接',
      });
    } else {
      // 请求配置出错
      console.error('请求配置错误:', error.message);
      return Promise.reject({
        status: -1,
        message: error.message,
      });
    }
  }
);

export default apiClient;
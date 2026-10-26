import axios from 'axios';
import { ElMessage } from 'element-plus';

export const TOKEN_KEY = 'ersho.token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

/** 业务异常：后端 code !== 200 时抛出，msg 可直接展示给用户 */
export class BizError extends Error {
  constructor(message, code) {
    super(message);
    this.name = 'BizError';
    this.code = code;
  }
}

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.token = token;
  return config;
});

let redirecting = false;

function handleUnauthorized(message) {
  clearToken();
  if (redirecting) return;
  redirecting = true;
  ElMessage.warning(message || '登录已过期，请重新登录');
  const current = window.location.hash.replace(/^#/, '') || '/';
  const target = current.startsWith('/login')
    ? '#/login'
    : `#/login?redirect=${encodeURIComponent(current)}`;
  window.location.hash = target;
  window.setTimeout(() => {
    redirecting = false;
  }, 800);
}

http.interceptors.response.use(
  (response) => {
    const body = response.data;
    // 非统一返回体（例如静态资源、容器错误页）原样返回
    if (!body || typeof body !== 'object' || !('code' in body)) return body;

    if (body.code === 200) return body.object;

    const error = new BizError(body.msg || '操作失败', body.code);
    if (!response.config?.silent) ElMessage.error(error.message);
    return Promise.reject(error);
  },
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      handleUnauthorized(error.response?.data?.msg);
      return Promise.reject(new BizError('未登录或登录已过期', 401));
    }

    let message = '网络异常，请检查后端是否已启动';
    if (error.code === 'ECONNABORTED') message = '请求超时，请稍后重试';
    else if (status === 404) message = '接口不存在（404）';
    else if (status >= 500) message = '服务器开小差了（' + status + '）';
    else if (error.response?.data?.msg) message = error.response.data.msg;

    const bizError = new BizError(message, status);
    if (!error.config?.silent) ElMessage.error(message);
    return Promise.reject(bizError);
  }
);

export default http;

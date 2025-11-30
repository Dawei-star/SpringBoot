import axios from "axios";
import { ElMessage, ElLoading } from "element-plus";
import { useTokenStore } from "@/stores/token";
import { useUserInfoStore } from "@/stores/userInfo";
import router from "@/router";
import { getCache, setCache, clearCacheByPrefix } from "./cache";
import { refreshTokenService } from "@/api/user";

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000 // 增加超时时间
});

// 请求计数器，用于管理loading
let requestCount = 0;
let loadingInstance = null;

// 显示loading
function showLoading() {
  if (requestCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.3)'
    });
  }
  requestCount++;
}

// 隐藏loading
function hideLoading() {
  requestCount--;
  if (requestCount === 0 && loadingInstance) {
    loadingInstance.close();
    loadingInstance = null;
  }
}

instance.interceptors.request.use(
  config => {
    const tokenStore = useTokenStore();
    // 从store中获取token（store已经处理了格式清理）
    // 使用cleanToken getter确保获取的是清理后的token
    const token = tokenStore.cleanToken || tokenStore.token;

    if (token && token.trim()) {
      // 确保Token格式正确（再次清理，防止意外情况）
      const cleanToken = token.trim().replace(/[\r\n\t]/g, '');
      config.headers.Authorization = cleanToken;
    }

    // 检查是否需要缓存（仅GET请求）
    // useCache默认为true，只有明确设置为false时才禁用缓存
    const shouldUseCache = config.useCache !== false && config.method === 'get';
    
    if (shouldUseCache) {
      const cached = getCache(config.url, config.params);
      if (cached) {
        // 返回缓存的Promise
        return Promise.reject({
          __cached: true,
          data: cached
        });
      }
    }

    // 显示loading（排除某些请求）
    if (config.showLoading !== false) {
      showLoading();
    }

    return config
  },
  error => {
    hideLoading();
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  result => {
    hideLoading();

    // 处理缓存数据
    if (result.__cached) {
      return Promise.resolve(result.data);
    }

    // 检查响应格式
    if (!result.data || typeof result.data !== 'object') {
      console.error('[Response] 响应数据格式错误:', result.data)
      return Promise.reject({
        code: 500,
        message: '响应数据格式错误',
        data: null
      })
    }
    
    // 成功状态码范围：200-299
    const code = result.data.code;
    if (code >= 200 && code < 300) {
      // 缓存GET请求结果
      const config = result.config;
      if (config.method === 'get' && config.useCache !== false) {
        setCache(config.url, config.params, result.data);
      }
      return result.data
    }
    
    // code不在成功范围内，返回reject让调用方处理
    // 不自动显示消息，让调用方决定如何显示错误
    return Promise.reject(result.data)
  },
  error => {
    hideLoading();

    // 处理缓存数据
    if (error.__cached) {
      return Promise.resolve(error.data);
    }

    // 网络错误或响应错误
    if (error && error.response) {
      const status = error.response.status
      if (status === 401) {
        const config = error.config

        // 如果是刷新token的请求失败，直接跳转登录
        if (config?.url?.includes('/user/refresh')) {
          const tokenStore = useTokenStore()
          const userInfoStore = useUserInfoStore()
          tokenStore.removeToken()
          userInfoStore.removeUserInfo()
          clearCacheByPrefix('/api')
          ElMessage.error('登录已过期，请重新登录')
          setTimeout(() => {
            router.push('/login').catch(() => { })
          }, 300)
          return Promise.reject(error)
        }

        // 尝试刷新token
        if (!config._retry && !config._refreshing) {
          config._refreshing = true

          const tokenStore = useTokenStore()
          const currentToken = tokenStore.token

          if (!currentToken) {
            // 没有token，直接跳转登录
            config._retry = true
            config._refreshing = false
            const userInfoStore = useUserInfoStore()
            userInfoStore.removeUserInfo()
            clearCacheByPrefix('/api')
            ElMessage.error('请先登录')
            setTimeout(() => {
              router.push('/login').catch(() => { })
            }, 300)
            return Promise.reject(error)
          }

          // 尝试刷新token
          return refreshTokenService()
            .then(res => {
              // 成功状态码范围：200-299
              if (res && res.code >= 200 && res.code < 300 && res.data) {
                // 更新token
                tokenStore.setToken(res.data)
                // 更新请求头
                config.headers.Authorization = res.data
                config._refreshing = false
                // 重试原请求
                return instance(config)
              } else {
                throw new Error('Token刷新失败')
              }
            })
            .catch(refreshError => {
              // 刷新失败，清除token并跳转登录
              config._retry = true
              config._refreshing = false
              const tokenStore = useTokenStore()
              const userInfoStore = useUserInfoStore()
              tokenStore.removeToken()
              userInfoStore.removeUserInfo()
              clearCacheByPrefix('/api')
              ElMessage.error('登录已过期，请重新登录')
              setTimeout(() => {
                router.push('/login').catch(() => { })
              }, 300)
              return Promise.reject(refreshError)
            })
        } else if (config._refreshing) {
          // 正在刷新中，等待刷新完成
          return new Promise((resolve) => {
            const checkRefresh = setInterval(() => {
              if (!config._refreshing) {
                clearInterval(checkRefresh)
                config.headers.Authorization = useTokenStore().token
                resolve(instance(config))
              }
            }, 100)

            // 10秒超时
            setTimeout(() => {
              clearInterval(checkRefresh)
              resolve(Promise.reject(error))
            }, 10000)
          })
        } else {
          // 已经重试过，直接跳转登录
          if (!config._retry) {
            config._retry = true
            const tokenStore = useTokenStore()
            const userInfoStore = useUserInfoStore()
            tokenStore.removeToken()
            userInfoStore.removeUserInfo()
            clearCacheByPrefix('/api')
            ElMessage.error('登录已过期，请重新登录')
            setTimeout(() => {
              router.push('/login').catch(() => { })
            }, 300)
          }
        }
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status >= 500) {
        // 500错误：尝试从响应体中获取错误信息，如果没有则使用默认消息
        const errorMessage = error.response?.data?.message 
          || error.response?.data?.error 
          || '服务器内部错误，请稍后重试'
        ElMessage.error(errorMessage)
        // 确保返回一个包含错误信息的对象，方便调用方处理
        const errorData = {
          code: status,
          message: errorMessage,
          data: null,
          error: error.response?.data || error.message
        }
        return Promise.reject(errorData)
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// 导出清除缓存的方法
export function clearApiCache(url) {
  clearCacheByPrefix(url || '/api');
}

export default instance
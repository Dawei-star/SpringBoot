import axios from 'axios'
import router from '../router'
import { useTokenStore } from '../stores/token'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000, // 增加超时时间
})

// 简单的内存缓存（仅用于GET请求）
const cache = new Map()
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟

function getCacheKey(url, params) {
  return `${url}${JSON.stringify(params || {})}`
}

request.interceptors.request.use((config) => {
  const tokenStore = useTokenStore()
  if (tokenStore.token) {
    config.headers.Authorization = tokenStore.token
  }
  
  // 检查缓存（仅GET请求）
  if (config.method === 'get' && config.useCache !== false) {
    const cacheKey = getCacheKey(config.url, config.params)
    const cached = cache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
      // 返回缓存的Promise
      return Promise.reject({
        __cached: true,
        data: cached.data
      })
    }
  }
  
  return config
})

request.interceptors.response.use(
  (resp) => {
    // 处理缓存数据
    if (resp.__cached) {
      return Promise.resolve(resp.data)
    }
    
    const data = resp.data
    if (data && typeof data.code !== 'undefined') {
      // 成功状态码范围：200-299（新系统）或 code === 0（旧系统兼容）
      if ((data.code >= 200 && data.code < 300) || data.code === 0) {
        // 缓存GET请求结果
        const config = resp.config
        if (config.method === 'get' && config.useCache !== false) {
          const cacheKey = getCacheKey(config.url, config.params)
          cache.set(cacheKey, {
            data,
            timestamp: Date.now()
          })
        }
        return data
      }
      // 业务错误，不自动显示消息（让调用方决定）
      return Promise.reject(data)
    }
    return resp
  },
  (error) => {
    // 处理缓存数据
    if (error.__cached) {
      return Promise.resolve(error.data)
    }
    
    if (error.response) {
      const status = error.response.status
      const url = error.config?.url || ''
      
      // 对于GET请求的文章/分类等公开接口，401错误不跳转登录（允许匿名访问）
      const isPublicGetRequest = status === 401 && 
        error.config?.method === 'get' && 
        (url.includes('/article') || url.includes('/category') || url.includes('/album') || url.includes('/message'))
      
      if (isPublicGetRequest) {
        // 允许匿名访问，不显示错误消息，不跳转登录
        console.debug('[Request] 允许匿名访问公开接口:', url)
        return Promise.reject(error)
      }
      
      if (status === 401) {
        ElMessage.error('未登录或登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 导出清除缓存的方法
export function clearApiCache(url) {
  if (url) {
    for (const key of cache.keys()) {
      if (key.startsWith(url)) {
        cache.delete(key)
      }
    }
  } else {
    cache.clear()
  }
}

export default request

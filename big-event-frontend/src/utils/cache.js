/**
 * 请求缓存工具
 * 用于缓存API请求结果，减少重复请求
 */

const cache = new Map()
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

/**
 * 生成缓存key
 */
function generateKey(url, params) {
  const paramStr = params ? JSON.stringify(params) : ''
  return `${url}${paramStr}`
}

/**
 * 获取缓存
 */
export function getCache(url, params) {
  const key = generateKey(url, params)
  const cached = cache.get(key)
  
  if (!cached) return null
  
  // 检查是否过期
  if (Date.now() - cached.timestamp > CACHE_DURATION) {
    cache.delete(key)
    return null
  }
  
  return cached.data
}

/**
 * 设置缓存
 */
export function setCache(url, params, data) {
  const key = generateKey(url, params)
  cache.set(key, {
    data,
    timestamp: Date.now()
  })
}

/**
 * 清除缓存
 */
export function clearCache(url, params) {
  if (url) {
    const key = generateKey(url, params)
    cache.delete(key)
  } else {
    // 清除所有缓存
    cache.clear()
  }
}

/**
 * 清除指定前缀的缓存
 */
export function clearCacheByPrefix(prefix) {
  for (const key of cache.keys()) {
    if (key.startsWith(prefix)) {
      cache.delete(key)
    }
  }
}


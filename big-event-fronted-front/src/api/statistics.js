import request from './request'

/**
 * 记录文章访问
 */
export const recordView = (articleId) => {
  return request.post(`/statistics/view?articleId=${articleId}`)
}

/**
 * 获取文章访问量
 */
export const getViewCount = (articleId) => {
  return request.get(`/statistics/view?articleId=${articleId}`)
}

/**
 * 获取热门文章ID列表
 */
export const getHotArticles = (limit = 10) => {
  return request.get(`/statistics/hot?limit=${limit}`)
}

/**
 * 获取统计数据
 */
export const getStatistics = () => {
  return request.get('/statistics')
}


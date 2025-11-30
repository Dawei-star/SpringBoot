import request from './request'

export const fetchArticles = (params = {}) => {
  const query = new URLSearchParams()
  query.append('pageNum', params.pageNum || 1)
  query.append('pageSize', params.pageSize || 10)
  if (params.categoryId) query.append('categoryId', params.categoryId)
  if (params.state) query.append('state', params.state)
  if (params.keyword) query.append('keyword', params.keyword)
  return request.get(`/article?${query.toString()}`)
}

export const fetchArticleDetail = (id) => {
  const query = new URLSearchParams()
  query.append('id', id)
  return request.get(`/article/detail?${query.toString()}`)
}

// 公开搜索接口 - 搜索已发布的文章
export const searchArticles = (params = {}) => {
  const query = new URLSearchParams()
  query.append('pageNum', params.pageNum || 1)
  query.append('pageSize', params.pageSize || 10)
  if (params.keyword) query.append('keyword', params.keyword)
  if (params.categoryId) query.append('categoryId', params.categoryId)
  return request.get(`/article/search?${query.toString()}`)
}

// ========== 新增功能 ==========

// 文章点赞
export const likeArticle = (articleId) => {
  return request.post(`/article/like?articleId=${articleId}`)
}

// 获取文章点赞状态
export const getArticleLikeStatus = (articleId) => {
  return request.get(`/article/like/status?articleId=${articleId}`)
}

// 文章收藏
export const favoriteArticle = (articleId) => {
  return request.post(`/article/favorite?articleId=${articleId}`)
}

// 取消收藏
export const unfavoriteArticle = (articleId) => {
  return request.delete(`/article/favorite?articleId=${articleId}`)
}

// 获取收藏状态
export const getFavoriteStatus = (articleId) => {
  return request.get(`/article/favorite/status?articleId=${articleId}`)
}

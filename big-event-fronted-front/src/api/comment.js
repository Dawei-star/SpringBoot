import request from './request'

/**
 * 获取文章评论列表
 */
export const fetchComments = (articleId) => {
  return request.get(`/comment?articleId=${articleId}`)
}

/**
 * 添加评论
 */
export const addComment = (data) => {
  return request.post('/comment', data)
}

/**
 * 点赞评论
 */
export const likeComment = (id) => {
  return request.post(`/comment/like?id=${id}`)
}

/**
 * 删除评论
 */
export const deleteComment = (id) => {
  return request.delete(`/comment?id=${id}`)
}

/**
 * 获取文章评论数量
 */
export const getCommentCount = (articleId) => {
  return request.get(`/comment/count?articleId=${articleId}`)
}


import request from './request'

/**
 * 发送聊天消息
 */
export const sendChatMessage = (content) => {
  return request.post('/chat/send', { content })
}

/**
 * 获取聊天历史
 */
export const getChatHistory = () => {
  return request.get('/chat/history', { useCache: false })
}


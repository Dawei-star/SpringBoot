import request from '@/utils/request'

export const messageListService = (params, useCache = true) => {
  const config = {
    params: params || {},
    useCache: useCache
  }
  return request.get('/message/list', config)
}

export const messageAddService = (data) => {
  return request.post('/message', data)
}

export const messageLikeService = (id) => {
  return request.post('/message/like?id=' + id)
}

export const messageDeleteService = (id) => {
  return request.delete('/message?id=' + id)
}


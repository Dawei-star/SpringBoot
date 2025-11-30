import request from '@/utils/request'

export const operationLogListService = (params) => {
  return request.get('/operation/log/list', { params })
}



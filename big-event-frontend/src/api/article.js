import request from "@/utils/request";

export const articleCategoryListService = (useCache = true) => {
  const config = {
    useCache: useCache
  }
  return request.get('/category', config)
}

export const articleCategoryAddService = (categoryData) => {
  return request.post('/category', categoryData)
}

export const articleCategoryUpdateService = (categoryData) => {
  return request.put('/category', categoryData)
}

export const articleCategoryDeleteService = (id) => {
  return request.delete('/category?id=' + id)
}

export const articleListService = (params, useCache = true) => {
  const config = {
    params: params || {},
    useCache: useCache
  }
  return request.get('/article', config)
}

// 搜索文章
export const articleSearchService = (params) => {
  return request.get('/article/search', { params })
}

export const articleAddService = (articleData) => {
  return request.post('/article', articleData)
}

export const articleUpdateService = (articleData) => {
  return request.put('/article', articleData)
}

export const articleDeleteService = (id) => {
  return request.delete('/article?id=' + id)
}

export const articleDetailService = (id) => {
  return request.get('/article/detail', { params: { id } })
}


import request from './request'

export const fetchCategories = () => request.get('/category')

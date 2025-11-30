import request from './request'

/**
 * 获取相册列表
 */
export const fetchAlbums = () => {
  return request.get('/album')
}

/**
 * 获取相册详情（包含图片列表）
 */
export const fetchAlbumDetail = (id) => {
  return request.get(`/album/${id}`)
}


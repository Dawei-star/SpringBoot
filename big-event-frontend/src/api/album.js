import request from "@/utils/request";

export const getAlbumList = (useCache = true) => {
  const config = {
    useCache: useCache
  }
  return request.get('/album', config)
}

export const getAlbumById = (id) => {
  return request.get(`/album/${id}`)
}

export const addAlbum = (albumData) => {
  return request.post('/album', albumData)
}

export const updateAlbum = (albumData) => {
  return request.put('/album', albumData)
}

export const deleteAlbum = (id) => {
  return request.delete(`/album/${id}`)
}


import request from "@/utils/request";

export const userRegisterService = (registerData) => {
  const params = new URLSearchParams();
  for (const key in registerData) {
    params.append(key, registerData[key])
  }
  return request.post('/user/register', params)
}

export const userLoginService = (loginData) => {
  const params = new URLSearchParams();
  for (const key in loginData) {
    if (loginData[key] !== undefined && loginData[key] !== null) {
      // 将布尔值转换为字符串
      const value = loginData[key];
      params.append(key, typeof value === 'boolean' ? String(value) : value)
    }
  }
  // 确保Content-Type正确设置为application/x-www-form-urlencoded
  return request.post('/user/login', params, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 刷新Token
 */
export const refreshTokenService = () => {
  return request.post('/user/refresh')
}

export const userInfoService = () => {
  return request.get('/user/userInfo')
}

export const userInfoUpdateService = (userInfoData) => {
  return request.put('/user/update', userInfoData)
}

export const userAvatarUpdateService = (avatarUrl) => {
  const urlSearchParams = new URLSearchParams();
  urlSearchParams.append('avatarUrl', avatarUrl)
  return request.patch('/user/updateAvatar', urlSearchParams)
}

export const userPwdUpdateService = (pwdData) => {
  return request.patch('/user/updatePwd', pwdData)
}

export const getUserList = (useCache = true) => {
  const config = {
    useCache: useCache
  }
  return request.get('/user/list', config)
}

export const updateUserRole = (id, role) => {
  const params = new URLSearchParams()
  params.append('id', id)
  params.append('role', role)
  return request.patch('/user/updateRole', params)
}

export const updateUserByAdmin = (userData) => {
  return request.put('/user/updateByAdmin', userData)
}


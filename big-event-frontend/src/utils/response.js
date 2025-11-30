/**
 * 统一响应处理工具
 * 用于处理后端返回的统一响应格式 {code, message, data}
 */

/**
 * 检查响应是否成功
 * @param {Object} res - 响应对象
 * @returns {boolean} 是否成功
 */
export function isSuccess(res) {
  return res && typeof res === 'object' && res.code >= 200 && res.code < 300
}

/**
 * 获取响应数据
 * @param {Object} res - 响应对象
 * @returns {*} 响应数据，如果失败返回null
 */
export function getData(res) {
  if (isSuccess(res) && res.data !== undefined) {
    return res.data
  }
  return null
}

/**
 * 获取响应消息
 * @param {Object} res - 响应对象
 * @param {string} defaultMessage - 默认消息
 * @returns {string} 响应消息
 */
export function getMessage(res, defaultMessage = '操作成功') {
  if (res && res.message) {
    return res.message
  }
  return defaultMessage
}

/**
 * 获取错误消息
 * @param {Error|Object} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @returns {string} 错误消息
 */
export function getErrorMessage(error, defaultMessage = '操作失败') {
  if (error && typeof error === 'object') {
    // 如果是响应对象（包含code和message）
    if (error.code !== undefined && error.message) {
      return error.message
    }
    // 如果是Error对象
    if (error.message) {
      return error.message
    }
  }
  return defaultMessage
}

/**
 * 处理API响应
 * @param {Promise} apiCall - API调用Promise
 * @param {Object} options - 选项
 * @param {Function} options.onSuccess - 成功回调
 * @param {Function} options.onError - 错误回调
 * @param {string} options.defaultErrorMessage - 默认错误消息
 * @returns {Promise} 处理后的Promise
 */
export async function handleResponse(apiCall, options = {}) {
  const {
    onSuccess,
    onError,
    defaultErrorMessage = '操作失败'
  } = options

  try {
    const res = await apiCall
    
    if (isSuccess(res)) {
      if (onSuccess) {
        onSuccess(res.data, res.message)
      }
      return { success: true, data: res.data, message: res.message }
    } else {
      const errorMsg = getMessage(res, defaultErrorMessage)
      if (onError) {
        onError(errorMsg, res)
      }
      return { success: false, error: errorMsg, response: res }
    }
  } catch (error) {
    const errorMsg = getErrorMessage(error, defaultErrorMessage)
    if (onError) {
      onError(errorMsg, error)
    }
    return { success: false, error: errorMsg, originalError: error }
  }
}


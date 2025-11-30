/**
 * 统一错误处理工具
 */

import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 处理API错误
 */
export function handleApiError(error, defaultMessage = '操作失败') {
  if (!error) {
    ElMessage.error(defaultMessage)
    return
  }

  // 处理响应错误
  if (error.response) {
    const status = error.response.status
    const message = error.response.data?.message || error.message

    switch (status) {
      case 400:
        ElMessage.error(message || '请求参数错误')
        break
      case 401:
        ElMessage.error('登录已过期，请重新登录')
        break
      case 403:
        ElMessage.error('没有权限执行此操作')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误，请稍后重试')
        break
      case 503:
        ElMessage.error('服务暂时不可用，请稍后重试')
        break
      default:
        ElMessage.error(message || defaultMessage)
    }
  } else if (error.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请检查网络连接')
  } else if (error.message) {
    ElMessage.error(error.message)
  } else {
    ElMessage.error(defaultMessage)
  }
}

/**
 * 处理业务错误
 */
export function handleBusinessError(error, defaultMessage = '操作失败') {
  if (error && error.message) {
    ElMessage.error(error.message)
  } else {
    ElMessage.error(defaultMessage)
  }
}

/**
 * 确认操作
 */
export function confirmAction(message, title = '温馨提示') {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
}

/**
 * 成功提示
 */
export function showSuccess(message = '操作成功') {
  ElMessage.success(message)
}

/**
 * 信息提示
 */
export function showInfo(message) {
  ElMessage.info(message)
}

/**
 * 警告提示
 */
export function showWarning(message) {
  ElMessage.warning(message)
}


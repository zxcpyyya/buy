/**
 * Vant Dialog 封装
 */
import { showConfirmDialog, showToast } from 'vant'

/**
 * 确认对话框
 * @param {Object} options 配置项
 * @returns {Promise<boolean>} 用户选择
 */
export const confirm = async (options = {}) => {
  const { title = '提示', message = '确定要执行此操作吗？', confirmButtonText = '确定', cancelButtonText = '取消' } = options
  
  try {
    await showConfirmDialog({
      title,
      message,
      confirmButtonText,
      cancelButtonText,
    })
    return true
  } catch {
    return false
  }
}

/**
 * 成功提示
 * @param {string} message 消息
 */
export const success = (message) => {
  showToast({ message, icon: 'success' })
}

/**
 * 失败提示
 * @param {string} message 消息
 */
export const error = (message) => {
  showToast({ message, icon: 'fail' })
}

/**
 * 普通提示
 * @param {string} message 消息
 */
export const tip = (message) => {
  showToast(message)
}

/**
 * 加载提示
 * @param {string} message 消息
 * @returns {Function} 关闭加载的函数
 */
export const loading = (message = '加载中...') => {
  const toast = showToast({
    message,
    forbidClick: true,
    loadingType: 'spinner',
    duration: 0,
  })
  
  return () => {
    toast.close()
  }
}

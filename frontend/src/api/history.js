import request from './request'

/**
 * 获取浏览历史
 */
export const getBrowseHistory = (limit = 10) => {
  return request.get('/history/browse', { params: { limit } })
}

/**
 * 清空浏览历史
 */
export const clearBrowseHistory = () => {
  return request.delete('/history/browse')
}

/**
 * 获取搜索历史
 */
export const getSearchHistory = (limit = 10) => {
  return request.get('/history/search', { params: { limit } })
}

/**
 * 清空搜索历史
 */
export const clearSearchHistory = () => {
  return request.delete('/history/search')
}

/**
 * 删除单条搜索历史
 */
export const deleteSearchHistory = (keyword) => {
  return request.delete(`/history/search/${encodeURIComponent(keyword)}`)
}

import request from '@/utils/request'

/** 获取所有分类 */
export function getAllCategories() {
  return request({ url: '/category/list', method: 'get' })
}

/** 获取分类树(管理端,含禁用) */
export function getCategoryTree() {
  return request({ url: '/category/tree', method: 'get' })
}

/** 获取子分类 */
export function getChildren(parentId) {
  return request({ url: '/category/children', method: 'get', params: { parentId } })
}

/** 新增分类 */
export function addCategory(data) {
  return request({ url: '/category/add', method: 'post', data })
}

/** 更新分类 */
export function updateCategory(data) {
  return request({ url: '/category/update', method: 'put', data })
}

/** 删除分类 */
export function deleteCategory(id) {
  return request({ url: `/category/delete/${id}`, method: 'delete' })
}

/** 启用/禁用分类 */
export function toggleCategoryStatus(id) {
  return request({ url: `/category/toggle-status/${id}`, method: 'put' })
}
import request from '@/utils/request'

/**
 * 获取所有分类
 */
export function getAllCategories() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

/**
 * 获取子分类
 */
export function getChildren(parentId) {
  return request({
    url: '/category/children',
    method: 'get',
    params: { parentId }
  })
}

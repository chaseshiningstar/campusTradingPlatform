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

import request from '@/utils/request'

/**
 * 获取物品评论列表
 */
export function getComments(itemId) {
  return request({
    url: `/comment/list/${itemId}`,
    method: 'get'
  })
}

/**
 * 添加评论
 */
export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 */
export function deleteComment(id) {
  return request({
    url: `/comment/delete/${id}`,
    method: 'delete'
  })
}

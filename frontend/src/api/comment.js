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

/**
 * 获取卖家收到的所有留言
 */
export function getMyMessages() {
  return request({
    url: '/comment/my-messages',
    method: 'get'
  })
}

/**
 * 管理员分页查询所有留言(支持时间筛选)
 */
export function getAdminComments(params) {
  return request({
    url: '/admin/comments',
    method: 'get',
    params
  })
}

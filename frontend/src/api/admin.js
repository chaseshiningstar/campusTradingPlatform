import request from '@/utils/request'

/**
 * 获取待审核物品列表
 */
export function getAuditItemList(params) {
  return request({
    url: '/admin/item/audit-list',
    method: 'get',
    params
  })
}

/**
 * 审核物品
 */
export function auditItem(data) {
  return request({
    url: '/admin/item/audit',
    method: 'post',
    data
  })
}

/**
 * 获取用户列表
 */
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 封禁/解封用户
 */
export function toggleUserStatus(id) {
  return request({
    url: `/admin/user/toggle-status/${id}`,
    method: 'put'
  })
}

/**
 * 管理员下架物品
 */
export function adminOfflineItem(id, reason) {
  return request({
    url: `/admin/item/offline/${id}`,
    method: 'put',
    params: { reason }
  })
}

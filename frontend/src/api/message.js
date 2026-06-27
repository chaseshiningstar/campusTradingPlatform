import request from '@/utils/request'

/**
 * 发送私信
 */
export function sendMessage(receiverId, content) {
  return request({
    url: '/message/send',
    method: 'post',
    data: { receiverId, content }
  })
}

/**
 * 获取与某用户的历史消息
 */
export function getMessageHistory(userId) {
  return request({
    url: `/message/history/${userId}`,
    method: 'get'
  })
}

/**
 * 获取会话列表
 */
export function getConversations() {
  return request({
    url: '/message/conversations',
    method: 'get'
  })
}

/**
 * 获取未读消息总数
 */
export function getUnreadCount() {
  return request({
    url: '/message/unread-count',
    method: 'get'
  })
}

/**
 * 标记与某用户的会话为已读
 */
export function markConversationRead(userId) {
  return request({
    url: `/message/read/${userId}`,
    method: 'put'
  })
}

import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 获取指定用户的公开信息(昵称/头像)
 */
export function getPublicUserInfo(userId) {
  return request({
    url: `/user/public/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/user/password',
    method: 'put',
    params: { oldPassword, newPassword }
  })
}

/**
 * 上传头像
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

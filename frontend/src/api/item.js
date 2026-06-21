import request from '@/utils/request'

/**
 * 获取物品列表
 */
export function getItemList(params) {
  return request({
    url: '/item/list',
    method: 'get',
    params
  })
}

/**
 * 获取物品详情
 */
export function getItemDetail(id) {
  return request({
    url: `/item/detail/${id}`,
    method: 'get'
  })
}

/**
 * 发布物品
 */
export function publishItem(data) {
  const formData = new FormData()
  formData.append('title', data.title)
  formData.append('description', data.description || '')
  formData.append('categoryId', data.categoryId)
  formData.append('price', data.price)
  if (data.originalPrice) {
    formData.append('originalPrice', data.originalPrice)
  }
  if (data.conditionLevel) {
    formData.append('conditionLevel', data.conditionLevel)
  }
  if (data.contactInfo) {
    formData.append('contactInfo', data.contactInfo)
  }

  // 添加图片
  if (data.images && data.images.length > 0) {
    data.images.forEach((file, index) => {
      formData.append('images', file)
    })
  }

  return request({
    url: '/item/publish',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新物品
 */
export function updateItem(id, data) {
  const formData = new FormData()
  formData.append('title', data.title)
  formData.append('description', data.description || '')
  formData.append('categoryId', data.categoryId)
  formData.append('price', data.price)
  if (data.originalPrice) {
    formData.append('originalPrice', data.originalPrice)
  }
  if (data.conditionLevel) {
    formData.append('conditionLevel', data.conditionLevel)
  }
  if (data.contactInfo) {
    formData.append('contactInfo', data.contactInfo)
  }

  if (data.images && data.images.length > 0) {
    data.images.forEach((file) => {
      formData.append('images', file)
    })
  }

  return request({
    url: `/item/update/${id}`,
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下架物品
 */
export function offlineItem(id) {
  return request({
    url: `/item/offline/${id}`,
    method: 'put'
  })
}

/**
 * 删除物品
 */
export function deleteItem(id) {
  return request({
    url: `/item/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取我的发布
 */
export function getMyItems(params) {
  return request({
    url: '/item/my-items',
    method: 'get',
    params
  })
}

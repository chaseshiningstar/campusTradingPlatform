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
 * AI生成标签
 */
export function generateTags(data) {
  return request({
    url: '/item/generate-tags',
    method: 'post',
    data,
    timeout: 60000 // 大模型API响应较慢,单独给60秒
  })
}

/**
 * AI识别图片生成商品信息(标题+描述+价格)
 */
export function generateFromImage(data) {
  return request({
    url: '/item/generate-from-image',
    method: 'post',
    data,
    timeout: 90000 // 图片识别耗时更长,给90秒
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
  if (data.size) {
    formData.append('size', data.size)
  }
  if (data.contactInfo) {
    formData.append('contactInfo', data.contactInfo)
  }
  if (data.tags) {
    formData.append('tags', data.tags)
  }

  if (data.images && data.images.length > 0) {
    data.images.forEach((file) => {
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
  if (data.size) {
    formData.append('size', data.size)
  }
  if (data.contactInfo) {
    formData.append('contactInfo', data.contactInfo)
  }
  if (data.tags) {
    formData.append('tags', data.tags)
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
 * 标记物品为已售出
 */
export function markAsSold(id) {
  return request({
    url: `/item/sold/${id}`,
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

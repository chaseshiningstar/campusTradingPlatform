<template>
  <div class="detail-page">
    <el-row :gutter="20">
      <!-- 物品图片 -->
      <el-col :md="12">
        <el-card>
          <div class="image-gallery">
            <el-carousel height="400px" indicator-position="outside" arrow="always">
              <el-carousel-item v-for="(img, index) in itemImages" :key="index">
                <img :src="img" alt="物品图片" class="carousel-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-card>
      </el-col>

      <!-- 物品信息 -->
      <el-col :md="12">
        <el-card>
          <h2>{{ item.title }}</h2>
          <div class="price">¥{{ item.price }}</div>
          <div class="original-price" v-if="item.originalPrice">原价: ¥{{ item.originalPrice }}</div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="新旧程度">{{ getConditionText(item.conditionLevel) }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ getCategoryName(item.categoryId) }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ formatTime(item.publishTime) }}</el-descriptions-item>
            <el-descriptions-item label="浏览次数">{{ item.viewCount }}</el-descriptions-item>
          </el-descriptions>

          <div class="description" v-if="item.description">
            <h4>物品描述</h4>
            <p>{{ item.description }}</p>
          </div>

          <div class="seller-info" v-if="item.sellerId">
            <h4>卖家信息</h4>
            <div class="seller-card" @click="goToChat">
              <el-avatar :size="40" :src="sellerAvatar || '/uploads/avatars/default.jpg'" />
              <div class="seller-meta">
                <span class="seller-name">{{ sellerNickname || '卖家' }}</span>
                <span class="seller-hint">点击发起私信</span>
              </div>
            </div>
          </div>

          <div class="actions">
            <el-button
              type="primary"
              size="large"
              @click="goToChat"
              :disabled="!isLoggedIn || isSeller"
            >
              {{ isSeller ? '自己的物品' : '私信卖家' }}
            </el-button>
            <el-button size="large" @click="handleComment">留言咨询</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评论列表 -->
    <el-card class="comment-section">
      <h3>留言咨询 ({{ comments.length }})</h3>

      <!-- 发表评论 -->
      <div class="comment-form" v-if="isLoggedIn">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="输入您的留言..."
        />
        <el-button type="primary" @click="submitComment" style="margin-top: 10px">
          发表留言
        </el-button>
      </div>
      <el-alert v-else title="请先登录后再评论" type="warning" :closable="false" />

      <!-- 评论列表（嵌套显示） -->
      <div class="comment-list">
        <template v-for="comment in threadedComments" :key="comment.id">
          <!-- 一级评论 -->
          <div class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="40" :src="comment.avatar || '/uploads/avatars/default.jpg'" />
            </div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="username">{{ comment.nickname || comment.username }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <el-button text size="small" @click="startReply(comment)">回复</el-button>
                <el-button
                  text size="small" type="danger"
                  v-if="canDelete(comment)"
                  @click="handleDelete(comment.id)"
                >删除</el-button>
              </div>

              <!-- 回复表单 -->
              <div v-if="replyingToId === comment.id" class="reply-form">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  :placeholder="'回复 @' + (comment.nickname || comment.username)"
                />
                <div class="reply-form-actions">
                  <el-button size="small" type="primary" @click="submitReply(comment)">回复</el-button>
                  <el-button size="small" @click="cancelReply">取消</el-button>
                </div>
              </div>

              <!-- 子回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="replies">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <div class="comment-avatar">
                    <el-avatar :size="32" :src="reply.avatar || '/uploads/avatars/default.jpg'" />
                  </div>
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="username">{{ reply.nickname || reply.username }}</span>
                      <span v-if="reply.replyToUserNickname" class="reply-to">
                        <el-icon><Right /></el-icon> @{{ reply.replyToUserNickname }}
                      </span>
                      <span class="time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <p class="comment-text">{{ reply.content }}</p>
                    <div class="comment-actions">
                      <el-button text size="small" @click="startReply(reply)">回复</el-button>
                      <el-button
                        text size="small" type="danger"
                        v-if="canDelete(reply)"
                        @click="handleDelete(reply.id)"
                      >删除</el-button>
                    </div>

                    <!-- 回复子评论的表单 -->
                    <div v-if="replyingToId === reply.id" class="reply-form">
                      <el-input
                        v-model="replyContent"
                        type="textarea"
                        :rows="2"
                        :placeholder="'回复 @' + (reply.nickname || reply.username)"
                      />
                      <div class="reply-form-actions">
                        <el-button size="small" type="primary" @click="submitReply(reply)">回复</el-button>
                        <el-button size="small" @click="cancelReply">取消</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-if="comments.length === 0" description="暂无留言" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItemDetail } from '@/api/item'
import { getComments, addComment, deleteComment } from '@/api/comment'
import { getAllCategories } from '@/api/category'
import { getPublicUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref({})
const comments = ref([])
const categories = ref([])
const itemImages = ref([])
const commentContent = ref('')
const sellerNickname = ref('')
const sellerAvatar = ref('')

// 回复状态
const replyingToId = ref(null)
const replyContent = ref('')

const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUserId = computed(() => userStore.userInfo?.id)
const isAdmin = computed(() => userStore.isAdmin)
const isSeller = computed(() => currentUserId.value && item.value?.sellerId === currentUserId.value)

// 构建嵌套评论结构
const threadedComments = computed(() => {
  const topLevel = comments.value.filter(c => !c.parentId || c.parentId === 0)
  const children = comments.value.filter(c => c.parentId && c.parentId > 0)
  return topLevel.map(parent => ({
    ...parent,
    replies: children.filter(c => c.parentId === parent.id)
  }))
})

// 加载物品详情
const loadItemDetail = async () => {
  try {
    const res = await getItemDetail(route.params.id)
    item.value = res.data

    // 设置物品图片
    if (res.data.images && res.data.images.length > 0) {
      itemImages.value = res.data.images
    } else {
      itemImages.value = ['/uploads/items/default.jpg']
    }

    // 加载卖家公开信息
    if (res.data.sellerId) {
      try {
        const sellerRes = await getPublicUserInfo(res.data.sellerId)
        sellerNickname.value = sellerRes.data?.nickname || sellerRes.data?.username || ''
        sellerAvatar.value = sellerRes.data?.avatar || ''
      } catch (e) {
        // 静默失败
      }
    }
  } catch (error) {
    console.error('加载物品详情失败:', error)
  }
}

// 跳转到与卖家的私信会话
const goToChat = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发起私信')
    router.push('/login')
    return
  }
  if (isSeller.value) {
    ElMessage.info('不能给自己发私信')
    return
  }
  const sellerId = item.value?.sellerId
  if (!sellerId) return
  router.push(`/chat/${sellerId}`)
}

// 加载评论
const loadComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    await addComment({
      itemId: route.params.id,
      content: commentContent.value
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadComments()
  } catch (error) {
    console.error('评论失败:', error)
  }
}

// 获取新旧程度文本
const getConditionText = (level) => {
  const map = { 1: '全新', 2: '九成新', 3: '八成新', 4: '七成新', 5: '六成新及以下' }
  return map[level] || '未知'
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.categoryName : ''
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleComment = () => {
  document.querySelector('.comment-form')?.scrollIntoView({ behavior: 'smooth' })
}

// 开始回复某条评论
const startReply = (comment) => {
  replyingToId.value = comment.id
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingToId.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (targetComment) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    // parentId: 如果回复的是一级评论就用其id, 否则用其parentId(保持在同一线程)
    const parentId = !targetComment.parentId || targetComment.parentId === 0
      ? targetComment.id
      : targetComment.parentId
    await addComment({
      itemId: route.params.id,
      content: replyContent.value,
      parentId: parentId,
      replyToUserId: targetComment.userId
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyingToId.value = null
    loadComments()
  } catch (error) {
    console.error('回复失败:', error)
  }
}

// 检查是否有删除权限(作者/卖家/管理员)
const canDelete = (comment) => {
  return currentUserId.value === comment.userId || isAdmin.value || isSeller.value
}

// 删除评论
const handleDelete = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条留言吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadItemDetail()
  loadComments()
  loadCategories()

  // 如果有 #comments hash，滚动到评论区
  if (route.hash === '#comments') {
    nextTick(() => {
      setTimeout(() => {
        document.querySelector('.comment-section')?.scrollIntoView({ behavior: 'smooth' })
      }, 300)
    })
  }
})
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.image-gallery {
  width: 100%;
  height: 400px;
  background: #f5f7fa;
  overflow: hidden;
}

.image-gallery :deep(.el-carousel__container) {
  height: 400px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.price {
  font-size: 32px;
  color: #f56c6c;
  font-weight: bold;
  margin: 20px 0;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
  margin-bottom: 20px;
}

.description, .contact, .seller-info {
  margin-top: 20px;
}

.description h4, .contact h4, .seller-info h4 {
  margin-bottom: 10px;
  color: #303133;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.seller-card:hover {
  background: #ecf5ff;
}

.seller-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.seller-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.seller-hint {
  font-size: 12px;
  color: #909399;
}

.actions {
  margin-top: 30px;
  display: flex;
  gap: 10px;
}

.comment-section {
  margin-top: 20px;
}

.comment-form {
  margin: 20px 0;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item .comment-avatar {
  float: left;
  margin-right: 12px;
}

.comment-body {
  overflow: hidden;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.comment-text {
  margin: 4px 0;
  color: #303133;
  line-height: 1.6;
}

.comment-actions {
  margin-top: 4px;
}

.reply-to {
  font-size: 12px;
  color: #409eff;
}

/* 子回复容器 */
.replies {
  margin-top: 10px;
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.reply-item:last-child {
  padding-bottom: 0;
}

.reply-item .comment-body {
  flex: 1;
}

/* 回复表单 */
.reply-form {
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.reply-form-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>

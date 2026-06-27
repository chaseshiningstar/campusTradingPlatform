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

          <div class="contact" v-if="showContact && item.contactInfo">
            <h4>联系方式</h4>
            <p>{{ item.contactInfo }}</p>
          </div>

          <div class="actions">
            <el-button type="primary" size="large" @click="showContact = true" :disabled="!item.contactInfo">
              {{ showContact ? '已显示联系方式' : '联系卖家' }}
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
          placeholder="输入您的评论..."
        />
        <el-button type="primary" @click="submitComment" style="margin-top: 10px">
          发表评论
        </el-button>
      </div>
      <el-alert v-else title="请先登录后再评论" type="warning" :closable="false" />

      <!-- 评论列表 -->
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.avatar || '/uploads/avatars/default.jpg'" />
          </div>
          <div class="comment-content">
            <div class="comment-header">
              <span class="username">{{ comment.nickname || comment.username }}</span>
              <span class="time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <p>{{ comment.content }}</p>
          </div>
        </div>
        <el-empty v-if="comments.length === 0" description="暂无评论" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getItemDetail } from '@/api/item'
import { getComments, addComment } from '@/api/comment'
import { getAllCategories } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const userStore = useUserStore()

const item = ref({})
const comments = ref([])
const categories = ref([])
const itemImages = ref([])
const commentContent = ref('')
const showContact = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)

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
  } catch (error) {
    console.error('加载物品详情失败:', error)
  }
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

onMounted(() => {
  loadItemDetail()
  loadComments()
  loadCategories()
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

.description, .contact {
  margin-top: 20px;
}

.description h4, .contact h4 {
  margin-bottom: 10px;
  color: #303133;
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
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}
</style>

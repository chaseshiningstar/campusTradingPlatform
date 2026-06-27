<template>
  <div class="home-page">
    <!-- 分类导航 -->
    <el-card class="category-card">
      <div class="category-list">
        <el-tag
          :type="activeCategory === null ? 'primary' : 'info'"
          class="category-tag"
          @click="selectCategory(null)"
        >
          全部
        </el-tag>
        <el-tag
          v-for="cat in categories"
          :key="cat.id"
          :type="activeCategory === cat.id ? 'primary' : 'info'"
          class="category-tag"
          @click="selectCategory(cat.id)"
        >
          {{ cat.categoryName }}
        </el-tag>
      </div>
    </el-card>

    <!-- 物品列表 -->
    <div class="item-section">
      <h3>{{ activeCategory ? getCategoryName(activeCategory) : '最新物品' }}</h3>

      <el-row :gutter="20">
        <el-col
          v-for="item in itemList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="item-card" shadow="hover" @click="goToDetail(item.id)">
            <div class="item-image">
              <img :src="item.coverImage || '/uploads/items/default.jpg'" alt="物品图片" />
            </div>
            <div class="item-info">
              <h4 class="item-title">{{ item.title }}</h4>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-meta">
                <span>{{ formatTime(item.publishTime) }}</span>
                <span>{{ item.viewCount }}次浏览</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="itemList.length === 0" description="暂无物品" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadItems"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getItemList } from '@/api/item'
import { getAllCategories } from '@/api/category'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const categories = ref([])
const itemList = ref([])
const activeCategory = ref(null)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data.filter(c => c.parentId === 0)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载物品列表
const loadItems = async () => {
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      status: 1 // 已发布
    }
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
    }

    const res = await getItemList(params)
    itemList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载物品列表失败:', error)
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  if (categoryId === null) {
    activeCategory.value = null
  } else {
    activeCategory.value = activeCategory.value === categoryId ? null : categoryId
  }
  currentPage.value = 1
  loadItems()
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.categoryName : ''
}

// 跳转到详情页
const goToDetail = (itemId) => {
  router.push(`/item/${itemId}`)
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.category-card {
  margin-bottom: 20px;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
  padding: 10px 20px;
  font-size: 14px;
}

.item-section h3 {
  margin-bottom: 20px;
  color: #303133;
}

.item-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
}

.item-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f7fa;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding-top: 10px;
}

.item-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>

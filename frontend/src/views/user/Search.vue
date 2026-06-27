<template>
  <div class="search-page">
    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <span class="filter-label">分类筛选：</span>
        <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="handleFilterChange" style="width: 200px">
          <el-option label="全部分类" :value="null" />
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.categoryName"
            :value="cat.id"
          />
        </el-select>
        <span class="filter-label">价格区间：</span>
        <el-input-number
          v-model="minPrice"
          :min="0"
          :precision="0"
          placeholder="最低价"
          controls-position="right"
          style="width: 130px"
        />
        <span class="filter-sep">—</span>
        <el-input-number
          v-model="maxPrice"
          :min="0"
          :precision="0"
          placeholder="最高价"
          controls-position="right"
          style="width: 130px"
        />
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card>
      <h3>搜索结果: "{{ route.query.keyword || '' }}"</h3>

      <el-row :gutter="20">
        <el-col v-for="item in itemList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="item-card" shadow="hover" @click="$router.push(`/item/${item.id}`)">
            <div class="item-image">
              <img :src="item.coverImage || '/uploads/items/default.jpg'" alt="物品图片" />
            </div>
            <div class="item-info">
              <h4 class="item-title">{{ item.title }}</h4>
              <div class="price">¥{{ item.price }}</div>
              <div class="item-meta">
                <span>{{ formatTime(item.publishTime) }}</span>
                <span>{{ item.viewCount }}次浏览</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="itemList.length === 0 && !loading" description="未找到相关物品" />

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
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getItemList } from '@/api/item'
import { getAllCategories } from '@/api/category'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const itemList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const categories = ref([])
const selectedCategory = ref(null)
const minPrice = ref(null)
const maxPrice = ref(null)

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = (res.data || []).filter(c => c.parentId === 0 && c.status === 1)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadItems = async () => {
  const keyword = route.query.keyword
  if (!keyword) return

  loading.value = true
  try {
    const params = {
      keyword,
      status: 1,
      current: currentPage.value,
      size: pageSize.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    if (minPrice.value !== null && minPrice.value !== undefined) {
      params.minPrice = minPrice.value
    }
    if (maxPrice.value !== null && maxPrice.value !== undefined) {
      params.maxPrice = maxPrice.value
    }
    const res = await getItemList(params)
    itemList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadItems()
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 监听路由 keyword 变化
watch(() => route.query.keyword, (newVal) => {
  if (newVal) {
    currentPage.value = 1
    loadItems()
  }
}, { immediate: true })

// 监听价格区间变化
watch([minPrice, maxPrice], () => {
  handleFilterChange()
})

// 初始加载分类
loadCategories()
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.filter-sep {
  color: #909399;
  font-size: 14px;
}

.search-page h3 {
  margin-bottom: 20px;
  color: #303133;
}

.item-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
}

.item-image {
  width: 100%;
  height: 180px;
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
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
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

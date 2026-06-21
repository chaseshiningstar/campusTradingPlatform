<template>
  <div class="category-page">
    <el-card>
      <h3>{{ categoryName }} - 物品列表</h3>
      <el-row :gutter="20">
        <el-col v-for="item in itemList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="item-card" shadow="hover" @click="$router.push(`/item/${item.id}`)">
            <img :src="'/uploads/items/default.jpg'" style="width: 100%; height: 150px; object-fit: cover" />
            <h4>{{ item.title }}</h4>
            <div class="price">¥{{ item.price }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="itemList.length === 0" description="该分类下暂无物品" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getItemList } from '@/api/item'
import { getAllCategories } from '@/api/category'

const route = useRoute()
const itemList = ref([])
const categoryName = ref('')

const loadData = async () => {
  try {
    const [itemsRes, catsRes] = await Promise.all([
      getItemList({ categoryId: route.params.id, status: 1 }),
      getAllCategories()
    ])
    itemList.value = itemsRes.data.records
    const cat = catsRes.data.find(c => c.id == route.params.id)
    categoryName.value = cat ? cat.categoryName : ''
  } catch (error) {
    console.error('加载失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-page h3 { margin-bottom: 20px; }
.item-card { cursor: pointer; margin-bottom: 20px; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; margin-top: 10px; }
</style>

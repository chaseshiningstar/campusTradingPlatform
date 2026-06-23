<template>
  <div class="search-page">
    <el-card>
      <h3>搜索结果: "{{ keyword }}"</h3>
      <el-row :gutter="20">
        <el-col v-for="item in itemList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="item-card" shadow="hover" @click="$router.push(`/item/${item.id}`)">
            <img :src="item.coverImage || '/uploads/items/default.jpg'" style="width: 100%; height: 150px; object-fit: cover" />
            <h4>{{ item.title }}</h4>
            <div class="price">¥{{ item.price }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="itemList.length === 0" description="未找到相关物品" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getItemList } from '@/api/item'

const route = useRoute()
const itemList = ref([])
const keyword = ref(route.query.keyword || '')

const loadItems = async () => {
  if (!keyword.value) return
  try {
    const res = await getItemList({ keyword: keyword.value, status: 1 })
    itemList.value = res.data.records
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.search-page h3 { margin-bottom: 20px; }
.item-card { cursor: pointer; margin-bottom: 20px; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; margin-top: 10px; }
</style>

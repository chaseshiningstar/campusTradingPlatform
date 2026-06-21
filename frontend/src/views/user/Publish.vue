<template>
  <div class="publish-page">
    <el-card>
      <h2>发布二手物品</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="物品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入物品标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="物品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>

        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>

        <el-form-item label="新旧程度">
          <el-select v-model="form.conditionLevel" placeholder="请选择" style="width: 200px">
            <el-option label="全新" :value="1" />
            <el-option label="九成新" :value="2" />
            <el-option label="八成新" :value="3" />
            <el-option label="七成新" :value="4" />
            <el-option label="六成新及以下" :value="5" />
          </el-select>
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="form.contactInfo" placeholder="手机号、微信等" />
        </el-form-item>

        <el-form-item label="物品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="详细描述物品的情况..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="物品图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :file-list="fileList"
            multiple
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传9张图片,第一张为封面</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            发布物品
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { publishItem } from '@/api/item'
import { getAllCategories } from '@/api/category'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const categories = ref([])
const fileList = ref([])

const form = reactive({
  title: '',
  categoryId: null,
  price: 0,
  originalPrice: null,
  conditionLevel: 2,
  contactInfo: '',
  description: '',
  images: []
})

const rules = {
  title: [{ required: true, message: '请输入物品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    categories.value = res.data.filter(c => c.parentId === 0)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 处理图片选择
const handleImageChange = (file, files) => {
  if (files.length > 9) {
    ElMessage.warning('最多上传9张图片')
    files.splice(9)
  }
  fileList.value = files
  form.images = files.map(f => f.raw)
}

// 提交发布
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    if (form.images.length === 0) {
      ElMessage.warning('请至少上传一张图片')
      return
    }

    loading.value = true
    await publishItem(form)
    ElMessage.success('发布成功,等待审核')
    router.push('/my-items')
  } catch (error) {
    console.error('发布失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
}

.publish-page h2 {
  margin-bottom: 30px;
  color: #303133;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>

<!-- 简化版动漫详情页 - 用于测试API调用 -->
<template>
  <div class="simple-anime-detail">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="animeData" class="anime-content">
      <h1>{{ animeData.title }}</h1>
      <p><strong>原标题：</strong>{{ animeData.originalTitle }}</p>
      <p><strong>年份：</strong>{{ animeData.year }}</p>
      <p><strong>季节：</strong>{{ animeData.season }}</p>
      <p><strong>状态：</strong>{{ animeData.status }}</p>
      <p><strong>评分：</strong>{{ animeData.rating }}</p>
      <p><strong>观看次数：</strong>{{ animeData.viewCount }}</p>
      <p><strong>制作公司：</strong>{{ animeData.studio }}</p>
      <p><strong>导演：</strong>{{ animeData.director }}</p>
      
      <div class="description">
        <h3>简介</h3>
        <p>{{ animeData.description }}</p>
      </div>
      
      <div class="poster">
        <img :src="animeData.poster" :alt="animeData.title" />
      </div>
    </div>
    
    <div v-else class="error">
      <p>加载失败</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { animeAPI } from '@/services/api'

const route = useRoute()
const loading = ref(false)
const animeData = ref(null)

const fetchAnimeDetail = async () => {
  const animeId = route.params.id

  try {
    loading.value = true
    console.log('开始获取动漫详情，ID:', animeId)
    
    const response = await animeAPI.getById(animeId)
    console.log('获取到的动漫详情:', response)
    
    animeData.value = response
    
  } catch (error) {
    console.error('获取动漫详情失败:', error)
    ElMessage.error('获取动漫详情失败')
  } finally {
    loading.value = false
  }
}

// 清理HTML标签和实体编码的函数 - 简化版本
const cleanHtmlTags = (text) => {
  if (!text) return ''

  // 移除HTML标签
  let cleanText = text.replace(/<[^>]*>/g, '')

  // 直接替换常见的HTML实体编码
  cleanText = cleanText
    .replace(/&nbsp;/g, ' ')      // 空格
    .replace(/&amp;/g, '&')      // &符号
    .replace(/&lt;/g, '<')       // 小于号
    .replace(/&gt;/g, '>')       // 大于号
    .replace(/&quot;/g, '"')     // 双引号
    .replace(/&#39;/g, "'")      // 单引号
    .replace(/&apos;/g, "'")     // 单引号
    .replace(/&copy;/g, '©')     // 版权符号
    .replace(/&reg;/g, '®')      // 注册商标
    .replace(/&trade;/g, '™')    // 商标符号
    .replace(/&hellip;/g, '…')   // 省略号
    .replace(/&mdash;/g, '—')    // 长破折号
    .replace(/&ndash;/g, '–')    // 短破折号
    .replace(/&ldquo;/g, '“')    // 左双引号 “
    .replace(/&rdquo;/g, '”')    // 右双引号 ”
    .replace(/&lsquo;/g, '‘')    // 左单引号 ‘
    .replace(/&rsquo;/g, '’')    // 右单引号 ’

  // 清理多余的空白字符
  cleanText = cleanText.replace(/\s+/g, ' ').trim()

  return cleanText
}

onMounted(() => {
  fetchAnimeDetail()
})
</script>

<style scoped>
.simple-anime-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.loading {
  padding: 40px;
}

.anime-content h1 {
  color: #2c3e50;
  margin-bottom: 20px;
}

.anime-content p {
  margin: 10px 0;
  line-height: 1.6;
}

.description {
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.poster img {
  max-width: 300px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.error {
  text-align: center;
  padding: 40px;
  color: #e74c3c;
}
</style>

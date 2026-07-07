<!-- API测试页面 - 用于调试API连接问题 -->
<template>
  <div class="api-test">
    <h1>API连接测试</h1>
    
    <div class="test-section">
      <h2>基础连接测试</h2>
      <el-button @click="testHealth" type="primary">测试健康检查</el-button>
      <el-button @click="testCategories" type="success">测试分类API</el-button>
      <el-button @click="testAnimeList" type="info">测试动漫列表</el-button>
      <el-button @click="testAnimeDetail" type="warning">测试动漫详情</el-button>
    </div>

    <div class="results">
      <h3>测试结果：</h3>
      <pre>{{ results }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { animeAPI, categoryAPI, testAPI } from '@/services/api'

const results = ref('')

const addResult = (test, result) => {
  const timestamp = new Date().toLocaleTimeString()
  results.value += `[${timestamp}] ${test}:\n${JSON.stringify(result, null, 2)}\n\n`
}

const testHealth = async () => {
  try {
    const result = await testAPI.health()
    addResult('健康检查', { success: true, data: result })
  } catch (error) {
    addResult('健康检查', { success: false, error: error.message })
  }
}

const testCategories = async () => {
  try {
    const result = await categoryAPI.getAll()
    addResult('分类API', { success: true, count: result.length, data: result })
  } catch (error) {
    addResult('分类API', { success: false, error: error.message })
  }
}

const testAnimeList = async () => {
  try {
    const result = await animeAPI.getAll(0, 5)
    addResult('动漫列表', { success: true, data: result })
  } catch (error) {
    addResult('动漫列表', { success: false, error: error.message })
  }
}

const testAnimeDetail = async () => {
  try {
    const result = await animeAPI.getById(1)
    addResult('动漫详情', { success: true, data: result })
  } catch (error) {
    addResult('动漫详情', { success: false, error: error.message })
  }
}
</script>

<style scoped>
.api-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-section {
  margin: 20px 0;
}

.test-section .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

.results {
  margin-top: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.results pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 500px;
  overflow-y: auto;
  background: white;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style>

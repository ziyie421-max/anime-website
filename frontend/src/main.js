// 主入口文件 - 配置Vue应用的核心依赖
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

// 导入路由
import router from './router'

// 导入状态管理
import pinia from './store'

// 导入Element Plus和中文语言包
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 导入动态favicon工具
import { watchThemeChange } from './utils/favicon.js'

// 创建Vue应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(router)
app.use(pinia)
app.use(ElementPlus, {
  locale: zhCn, // 配置中文语言包
})

// 挂载应用
app.mount('#app')

// 启动主题变化监听器（favicon会在主题初始化时设置）
watchThemeChange()

import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // 允许局域网访问 - 绑定到0.0.0.0使得局域网内的其他设备可以访问
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // 后端 API 代理：开发期前端走相对路径 /api，由 Vite 转发到本地 8080 后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 代理量子资源API请求
      '/quantum-api': {
        target: 'https://cj.lziapi.com',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/quantum-api/, '/api.php/provide/vod'),
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
        }
      }
    }
  }
})

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    // element-plus 全量引入约 1.1MB（gzip 345KB），是当前产物的主要构成。
    // 阈值放到 1200 是为了让构建输出干净；要进一步压缩需上按需引入插件，属于后续优化。
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        // 按「长期不变 / 变化频繁」拆分：框架与图表库独立成块，
        // 业务代码改动时这几个大块的 hash 不变，浏览器缓存可以直接命中
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus', '@element-plus/icons-vue'],
          echarts: ['echarts']
        }
      }
    }
  }
})

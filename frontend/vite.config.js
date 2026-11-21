import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // 按需引入 Element Plus 组件与样式，避免全量打包 1.1MB
    Components({ resolvers: [ElementPlusResolver()] })
  ],
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
    // Element Plus 已改为按需引入，组件按页面拆块，无需再手动钉住 element 大块。
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        // 只钉住长期不变的框架与图表库；Element Plus 组件交给按需插件按引用拆分
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          echarts: ['echarts']
        }
      }
    }
  }
})

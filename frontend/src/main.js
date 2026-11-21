import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 组件由 unplugin-vue-components 按需自动引入，不再全量注册
// 命令式 API（ElMessage/ElMessageBox）的样式需单独引入
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/el-message-box.css'
import App from './App.vue'
import router from './router'
import permission from './directives/permission'
import './styles.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('permission', permission)
app.mount('#app')

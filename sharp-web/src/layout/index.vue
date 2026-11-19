<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-mark">智</div>
        <div>
          <div class="logo-title">锐客 CRM</div>
          <div class="logo-sub">AI 职业教育招生管理</div>
        </div>
      </div>
      <el-menu
        router
        :default-active="route.meta.activeMenu || route.path"
        background-color="#0f172a"
        text-color="#94a3b8"
        active-text-color="#fff"
      >
        <el-menu-item v-for="m in visibleMenus" :key="m.path" :index="m.path">
          <el-icon><component :is="resolveIcon(m.icon)" /></el-icon>{{ m.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">{{ route.meta.title || '首页' }}</div>
        <div class="header-right">
          <el-avatar :size="32" style="background:#2563eb">{{ (userInfo.name || 'U').charAt(0) }}</el-avatar>
          <span class="user-name">{{ userInfo.name || '用户' }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import * as ElIcons from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userInfo = userStore.userInfo

// 菜单配置：roles 为空表示所有登录用户可见，否则只有列出的角色可见
// 与后端 @RequireRole 标注保持一致，前端只做展示层收敛，真正的权限校验在服务端
const menus = [
  { path: '/dashboard', title: '工作台', icon: 'DataBoard' },
  { path: '/todo', title: '待办中心', icon: 'Bell' },
  { path: '/clue', title: '线索管理', icon: 'Tickets' },
  { path: '/business', title: '商机管理', icon: 'TrendCharts' },
  { path: '/customer', title: '客户管理', icon: 'UserFilled' },
  { path: '/contract', title: '合同回款', icon: 'Money' },
  { path: '/course', title: '课程管理', icon: 'Reading', roles: ['admin'] },
  { path: '/activity', title: '市场活动', icon: 'Calendar', roles: ['admin'] },
  { path: '/user', title: '用户管理', icon: 'User', roles: ['admin'] },
  { path: '/dept', title: '部门管理', icon: 'OfficeBuilding', roles: ['admin'] },
  { path: '/role', title: '角色管理', icon: 'Lock', roles: ['admin'] },
  { path: '/log', title: '操作日志', icon: 'Document', roles: ['admin'] }
]

const visibleMenus = computed(() => {
  const role = userInfo.roleLabel
  // 旧数据缺失角色时按最小权限展示，避免默认放开管理菜单
  return menus.filter((m) => !m.roles || m.roles.includes(role))
})

const resolveIcon = (name) => ElIcons[name]

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #0f172a; overflow-y: auto; }
.logo {
  height: 64px; display: flex; align-items: center; gap: 12px;
  padding: 0 20px; border-bottom: 1px solid #1e293b;
}
.logo-mark {
  width: 36px; height: 36px; border-radius: 8px; background: #2563eb;
  color: #fff; font-weight: 700; font-size: 19px; font-family: "Microsoft YaHei", sans-serif;
  display: flex; align-items: center; justify-content: center;
}
.logo-title { color: #fff; font-weight: 600; font-size: 15px; }
.logo-sub { color: #64748b; font-size: 11px; margin-top: 2px; }
.el-menu { border-right: none; }
.header {
  background: #fff; display: flex; align-items: center;
  justify-content: space-between; box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.breadcrumb { font-size: 16px; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-name { font-size: 14px; }
.main { padding: 20px; overflow-y: auto; }
</style>

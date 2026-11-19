import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'todo', component: () => import('@/views/Todo.vue'), meta: { title: '待办中心' } },
      { path: 'clue', component: () => import('@/views/Clue.vue'), meta: { title: '线索管理' } },
      { path: 'business', component: () => import('@/views/Business.vue'), meta: { title: '商机管理' } },
      { path: 'customer', component: () => import('@/views/Customer.vue'), meta: { title: '客户管理' } },
      { path: 'customer/:id', component: () => import('@/views/CustomerDetail.vue'), meta: { title: '客户详情', activeMenu: '/customer' } },
      { path: 'contract', component: () => import('@/views/Contract.vue'), meta: { title: '合同回款' } },
      { path: 'course', component: () => import('@/views/Course.vue'), meta: { title: '课程管理' } },
      { path: 'activity', component: () => import('@/views/Activity.vue'), meta: { title: '市场活动' } },
      { path: 'user', component: () => import('@/views/User.vue'), meta: { title: '用户管理' } },
      { path: 'dept', component: () => import('@/views/Dept.vue'), meta: { title: '部门管理' } },
      { path: 'role', component: () => import('@/views/Role.vue'), meta: { title: '角色管理' } },
      { path: 'log', component: () => import('@/views/Log.vue'), meta: { title: '操作日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  if (!to.meta.public && !localStorage.getItem('token')) {
    return '/login'
  }
  return true
})

export default router

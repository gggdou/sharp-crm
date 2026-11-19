<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="brand">
        <div class="brand-mark">智</div>
        <div>
          <h1>锐客 CRM 招生管理系统</h1>
          <p>AI 职业教育培训机构 · 客户关系管理</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" class="form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock"
                    show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="tip">
        <span>测试账号：zhangsan / zhangsan123</span>
        <el-divider direction="vertical" />
        <el-link type="primary" :underline="false" @click="openRegister">注册账号</el-link>
      </div>
    </div>

    <el-dialog v-model="reg.visible" title="注册新账号" width="420px">
      <el-form ref="regRef" :model="regForm" :rules="regRules" label-width="76px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="regForm.username" placeholder="2-20 位，登录用" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="regForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="regForm.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="regForm.phone" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="regForm.email" placeholder="用于账号找回" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reg.visible = false">取消</el-button>
        <el-button type="primary" :loading="reg.loading" @click="handleRegister">确定注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册弹窗
const regRef = ref()
const reg = reactive({ visible: false, loading: false })
const regForm = reactive({ username: '', password: '', name: '', phone: '', email: '' })
const regRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' },
             { min: 2, max: 20, message: '长度 2-20 位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
             { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function openRegister() {
  Object.assign(regForm, { username: '', password: '', name: '', phone: '', email: '' })
  reg.visible = true
}

async function handleRegister() {
  await regRef.value.validate()
  reg.loading = true
  try {
    await register({ ...regForm })
    ElMessage.success('注册成功，请使用新账号登录')
    form.username = regForm.username
    form.password = ''
    reg.visible = false
  } finally {
    reg.loading = false
  }
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #38bdf8 100%);
}
.login-panel {
  width: 420px; background: #fff; border-radius: 16px; padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .25);
}
.brand { display: flex; align-items: center; gap: 14px; margin-bottom: 36px; }
.brand-mark {
  width: 48px; height: 48px; border-radius: 12px; background: #2563eb;
  color: #fff; font-size: 25px; font-weight: 700; font-family: "Microsoft YaHei", sans-serif;
  display: flex; align-items: center; justify-content: center;
}
.brand h1 { font-size: 20px; font-weight: 700; }
.brand p { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.form :deep(.el-input__wrapper) { border-radius: 8px; }
.submit { width: 100%; height: 44px; border-radius: 8px; font-weight: 600; }
.tip { text-align: center; color: #94a3b8; font-size: 12px; margin-top: 20px; }
</style>

<template>
  <div class="page-card">
    <div class="page-header"><h2>客户管理</h2></div>
    <el-form :inline="true" :model="query">
      <el-form-item label="姓名"><el-input v-model="query.name" placeholder="客户姓名" clearable /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="query.phone" placeholder="手机号" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="客户姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="subject" label="意向学科">
        <template #default="{ row }">{{ subjectMap[row.subject] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="courseId" label="成交课程ID" />
      <el-table-column prop="userId" label="归属人ID" />
      <el-table-column prop="createTime" label="成交时间" width="170" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/customer/${row.id}`)">360 视图</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next"
                   :total="total" v-model:current-page="query.page" v-model:page-size="query.pageSize" @current-change="loadData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { customerApi } from '@/api'
const router = useRouter()
const subjectMap = { 1:'AI应用开发', 2:'AI大模型', 3:'AI鸿蒙', 4:'AI大数据', 5:'AI嵌入式', 6:'AI测试', 7:'AI运维' }
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ name:'', phone:'', page:1, pageSize:10 })
async function loadData() {
  loading.value = true
  try { const res = await customerApi.page({...query}); list.value = res.data?.rows || []; total.value = res.data?.total || 0 }
  finally { loading.value = false }
}
onMounted(loadData)
</script>

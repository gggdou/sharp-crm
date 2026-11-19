<template>
  <div class="page-card">
    <div class="page-header"><h2>操作日志</h2></div>
    <el-form :inline="true" :model="query">
      <el-form-item label="操作人"><el-input v-model="query.operateUserName" placeholder="操作人姓名" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="operateUserId" label="操作人ID" width="100" />
      <el-table-column prop="className" label="类名" width="220" show-overflow-tooltip />
      <el-table-column prop="methodName" label="方法名" width="160" />
      <el-table-column prop="methodParams" label="请求参数" min-width="180" show-overflow-tooltip />
      <el-table-column prop="costTime" label="耗时(ms)" width="100" />
      <el-table-column prop="operateTime" label="操作时间" width="170" />
    </el-table>
    <el-pagination style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next"
                   :total="total" v-model:current-page="query.page" v-model:page-size="query.pageSize" @current-change="loadData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { logApi } from '@/api'
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ operateUserName:'', page:1, pageSize:10 })
async function loadData() {
  loading.value = true
  try { const res = await logApi.page({...query}); list.value = res.data?.rows || []; total.value = res.data?.total || 0 }
  finally { loading.value = false }
}
onMounted(loadData)
</script>

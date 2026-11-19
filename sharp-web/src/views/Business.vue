<template>
  <div class="page-card">
    <div class="page-header"><h2>商机管理</h2></div>
    <el-form :inline="true" :model="query">
      <el-form-item label="客户姓名"><el-input v-model="query.name" placeholder="姓名" clearable /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="query.phone" placeholder="手机号" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-tabs v-model="tab" @tab-change="loadData">
      <el-tab-pane label="我的商机" name="mine" />
      <el-tab-pane label="商机公海" name="pool" />
    </el-tabs>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="客户姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="subject" label="意向学科" width="150">
        <template #default="{ row }">{{ subjectMap[row.subject] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }"><el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openFollow(row)">跟进</el-button>
          <el-button link type="warning" @click="assign(row)" v-if="row.status === 1" v-permission="['admin', 'business_operator']">分配</el-button>
          <el-button link type="info" @click="back(row)" v-if="row.userId" v-permission="['admin', 'business_operator']">踢回公海</el-button>
          <el-button link type="success" @click="toCustomer(row)">转客户</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next"
                   :total="total" v-model:current-page="query.page" v-model:page-size="query.pageSize" @current-change="loadData" />

    <el-dialog v-model="dialog.visible" title="跟进商机" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="跟进状态"><el-input-number v-model="form.trackStatus" :min="1" :max="5" /></el-form-item>
        <el-form-item label="跟进要点"><el-input v-model="form.keyItems" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="跟进内容"><el-input v-model="form.record" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible=false">取消</el-button>
        <el-button type="primary" @click="submitFollow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { businessApi } from '@/api'

const subjectMap = { 1:'AI智能应用开发(Java)', 2:'AI大模型开发(Python)', 3:'AI鸿蒙开发', 4:'AI大数据', 5:'AI嵌入式', 6:'AI测试', 7:'AI运维' }
const statusMap = { 1:'待分配', 2:'待跟进', 3:'跟进中', 4:'回收', 5:'转客户' }
const statusType = { 1:'info', 2:'warning', 3:'primary', 4:'danger', 5:'success' }

const tab = ref('mine'); const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ name:'', phone:'', page:1, pageSize:10 })
const dialog = reactive({ visible:false }); const form = reactive({})

async function loadData() {
  loading.value = true
  try {
    const res = tab.value === 'pool' ? await businessApi.pool({...query}) : await businessApi.page({...query})
    list.value = res.data?.rows || []; total.value = res.data?.total || 0
  } finally { loading.value = false }
}
function openFollow(row) { Object.assign(form, { ...row }); dialog.visible = true }
async function submitFollow() {
  await businessApi.follow(form); ElMessage.success('跟进成功'); dialog.visible = false; loadData()
}
async function assign(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入归属人用户ID', '分配商机', { inputPattern:/^\d+$/, inputErrorMessage:'数字ID' })
    await businessApi.assign(row.id, Number(value)); ElMessage.success('分配成功'); loadData()
  } catch(e) {}
}
async function back(row) { await businessApi.back(row.id); ElMessage.success('已踢回公海'); loadData() }
async function toCustomer(row) {
  await ElMessageBox.confirm('确认将该商机转为客户？', '提示')
  await businessApi.toCustomer(row.id); ElMessage.success('已转客户'); loadData()
}
onMounted(loadData)
</script>

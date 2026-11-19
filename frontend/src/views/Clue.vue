<template>
  <div class="page-card">
    <div class="page-header">
      <h2>线索管理</h2>
      <div>
        <el-upload :show-file-list="false" :http-request="handleImport" style="display:inline-block;margin-right:8px">
          <el-button type="success" plain>导入 Excel</el-button>
        </el-upload>
        <el-button type="warning" plain @click="handleExport">导出 Excel</el-button>
        <el-button type="primary" @click="openAdd">新增线索</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="姓名"><el-input v-model="query.name" placeholder="客户姓名" clearable /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="query.phone" placeholder="手机号" clearable /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
          <el-option label="待分配" :value="1" /><el-option label="跟进中" :value="2" />
          <el-option label="伪线索" :value="3" /><el-option label="已转商机" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>

    <el-tabs v-model="tab" @tab-change="loadData">
      <el-tab-pane label="我的线索" name="mine" />
      <el-tab-pane label="线索公海" name="pool" />
    </el-tabs>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="channel" label="渠道" width="100">
        <template #default="{ row }">{{ row.channel === 1 ? '线上活动' : '推广介绍' }}</template>
      </el-table-column>
      <el-table-column prop="subject" label="意向学科" width="110">
        <template #default="{ row }">{{ subjectMap[row.subject] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="level" label="意向等级" width="100">
        <template #default="{ row }">{{ levelMap[row.level] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }"><el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openFollow(row)">跟进</el-button>
          <el-button link type="warning" @click="handleAssign(row)" v-if="row.status === 1" v-permission="['admin', 'clue_operator']">分配</el-button>
          <el-button link type="success" @click="handleToBusiness(row)" v-if="row.status === 2">转商机</el-button>
          <el-button link type="danger" @click="handleFalse(row)" v-if="row.status === 2">伪线索</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next"
                   :total="total" v-model:current-page="query.page" v-model:page-size="query.pageSize"
                   @current-change="loadData" />

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="form.channel" style="width:100%">
            <el-option label="线上活动" :value="1" /><el-option label="推广介绍" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向学科">
          <el-select v-model="form.subject" style="width:100%">
            <el-option v-for="(v,k) in subjectMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向等级">
          <el-select v-model="form.level" style="width:100%">
            <el-option v-for="(v,k) in levelMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" v-if="dialog.mode === 'follow'"><el-input v-model="form.record" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitDialog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clueApi, userApi } from '@/api'

const subjectMap = { 1:'AI智能应用开发(Java)', 2:'AI大模型开发(Python)', 3:'AI鸿蒙开发', 4:'AI大数据', 5:'AI嵌入式', 6:'AI测试', 7:'AI运维' }
const levelMap = { 1:'近期学习', 2:'打算学习', 3:'进行了解', 4:'打酱油' }
const statusMap = { 1:'待分配', 2:'跟进中', 3:'伪线索', 4:'已转商机' }
const statusType = { 1:'info', 2:'primary', 3:'danger', 4:'success' }

const tab = ref('mine')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ name:'', phone:'', status:null, page:1, pageSize:10 })
const dialog = reactive({ visible:false, title:'', mode:'add' })
const form = reactive({})

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    const res = tab.value === 'pool' ? await clueApi.pool(params) : await clueApi.page(params)
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openAdd() {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { channel:1, subject:1, level:1 })
  dialog.title = '新增线索'; dialog.mode = 'add'; dialog.visible = true
}
function openFollow(row) {
  Object.assign(form, { ...row })
  dialog.title = '跟进线索'; dialog.mode = 'follow'; dialog.visible = true
}
async function submitDialog() {
  if (dialog.mode === 'add') await clueApi.add(form)
  else await clueApi.follow(form)
  ElMessage.success('操作成功'); dialog.visible = false; loadData()
}
async function handleAssign(row) {
  const { data: users } = await userApi.list()
  try {
    const { value } = await ElMessageBox.prompt('请输入归属人用户ID（可在用户管理查看）', '分配线索', { inputPattern: /^\d+$/, inputErrorMessage: '请输入数字ID' })
    await clueApi.assign(row.id, Number(value)); ElMessage.success('分配成功'); loadData()
  } catch (e) { /* 取消 */ }
}
async function handleToBusiness(row) {
  await ElMessageBox.confirm('确认将该线索转为商机？', '提示')
  await clueApi.toBusiness(row.id); ElMessage.success('已转商机'); loadData()
}
async function handleFalse(row) {
  await ElMessageBox.confirm('确认标记为伪线索？', '提示')
  await clueApi.falseFollow(row.id, { reason:'无效线索', remark:'电话无法接通' })
  ElMessage.success('已标记'); loadData()
}
async function handleImport({ file }) {
  const res = await clueApi.import(file); ElMessage.success(res.msg); loadData()
}
function handleExport() { window.location.href = clueApi.exportUrl }

onMounted(loadData)
</script>

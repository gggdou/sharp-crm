<template>
  <div class="page-card">
    <div class="page-header">
      <h2>合同回款</h2>
      <el-button type="primary" @click="openCreate">签订合同</el-button>
    </div>

    <el-form :inline="true" :model="query">
      <el-form-item label="合同编号">
        <el-input v-model="query.contractNo" placeholder="合同编号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(label, value) in STATUS" :key="value" :label="label" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="contractNo" label="合同编号" width="160" />
      <el-table-column prop="customerName" label="客户" min-width="110" />
      <el-table-column label="合同金额" width="130" align="right">
        <template #default="{ row }">¥{{ money(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="已回款" width="130" align="right">
        <template #default="{ row }">¥{{ money(row.paidAmount) }}</template>
      </el-table-column>
      <el-table-column label="回款进度" width="170">
        <template #default="{ row }">
          <el-progress :percentage="progressOf(row)" :stroke-width="6" />
        </template>
      </el-table-column>
      <el-table-column label="付款方式" width="100" align="center">
        <template #default="{ row }">{{ row.payType === 2 ? '分期' : '一次性' }}</template>
      </el-table-column>
      <el-table-column label="期数" width="70" align="center">
        <template #default="{ row }">{{ row.periodCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ STATUS[row.status] || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="signDate" label="签订日期" width="120" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="danger" @click="handleRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      layout="total,prev,pager,next"
      :total="total"
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      @current-change="loadData"
    />

    <el-dialog v-model="create.visible" title="签订合同" width="500px">
      <el-form :model="create.form" label-width="90px">
        <el-form-item label="客户" required>
          <el-select v-model="create.form.customerId" filterable placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.name}（${c.phone || '无手机号'}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同金额" required>
          <el-input-number v-model="create.form.amount" :min="0.01" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-radio-group v-model="create.form.payType">
            <el-radio :value="1">一次性</el-radio>
            <el-radio :value="2">分期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="create.form.payType === 2" label="分期数">
          <el-input-number v-model="create.periods" :min="2" :max="12" style="width: 100%" />
        </el-form-item>
        <el-form-item label="签订日期">
          <el-date-picker v-model="create.form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="create.form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div class="tip" v-if="create.form.amount > 0">
        签订后自动生成 {{ create.form.payType === 2 ? create.periods : 1 }} 期回款计划，
        每期金额合计严格等于合同总额
      </div>
      <template #footer>
        <el-button @click="create.visible = false">取消</el-button>
        <el-button type="primary" :loading="create.loading" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detail.visible" title="合同详情" size="680px">
      <div v-if="detail.data" v-loading="detail.loading">
        <div class="detail-head">
          <div>
            <h3>{{ detail.data.contract.contractNo }}</h3>
            <p class="sub">{{ detail.data.customerName }} · 签订于 {{ detail.data.contract.signDate }}</p>
          </div>
          <el-tag :type="statusType(detail.data.contract.status)">
            {{ STATUS[detail.data.contract.status] }}
          </el-tag>
        </div>

        <div class="metrics">
          <div class="metric"><b>¥{{ money(detail.data.contract.amount) }}</b><span>合同金额</span></div>
          <div class="metric"><b class="ok">¥{{ money(detail.data.paidAmount) }}</b><span>已回款</span></div>
          <div class="metric"><b class="warn">¥{{ money(detail.data.unpaidAmount) }}</b><span>未回款</span></div>
          <div class="metric"><b>{{ detail.data.progress }}%</b><span>回款进度</span></div>
        </div>

        <h4 class="section-title">回款计划</h4>
        <el-table :data="detail.data.payments" border size="small">
          <el-table-column prop="period" label="期次" width="60" align="center" />
          <el-table-column label="计划金额" width="110" align="right">
            <template #default="{ row }">¥{{ money(row.planAmount) }}</template>
          </el-table-column>
          <el-table-column prop="planDate" label="计划日期" width="110" />
          <el-table-column label="实收金额" width="110" align="right">
            <template #default="{ row }">
              <span :class="{ paid: row.actualAmount }">{{ row.actualAmount ? '¥' + money(row.actualAmount) : '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="actualDate" label="实收日期" width="110" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="payType(row.status)" size="small">{{ PAY[row.status] || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === 3" @click="openPay(row)">登记</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <el-dialog v-model="pay.visible" title="回款登记" width="420px">
      <el-form :model="pay.form" label-width="90px">
        <el-form-item label="期次">{{ pay.form.period }} / 计划 ¥{{ money(pay.form.planAmount) }}</el-form-item>
        <el-form-item label="实收金额" required>
          <el-input-number v-model="pay.form.actualAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实收日期">
          <el-date-picker v-model="pay.form.actualDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="pay.form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pay.visible = false">取消</el-button>
        <el-button type="primary" :loading="pay.loading" @click="submitPay">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractApi, customerApi } from '@/api'

const STATUS = { 1: '草稿', 2: '生效中', 3: '已完成', 4: '已作废' }
const PAY = { 1: '待回款', 2: '部分回款', 3: '已回款', 4: '逾期' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const customers = ref([])
const query = reactive({ contractNo: '', status: null, page: 1, pageSize: 10 })

const create = reactive({
  visible: false,
  loading: false,
  periods: 3,
  form: {}
})
const detail = reactive({ visible: false, loading: false, data: null })
const pay = reactive({ visible: false, loading: false, form: {} })

function money(v) {
  const n = Number(v || 0)
  return n.toFixed(2)
}

function progressOf(row) {
  const amount = Number(row.amount || 0)
  const paid = Number(row.paidAmount || 0)
  if (amount <= 0) return 0
  return Math.min(100, Math.round((paid / amount) * 100))
}

function statusType(s) {
  return s === 3 ? 'success' : s === 4 ? 'info' : s === 2 ? 'primary' : 'warning'
}

function payType(s) {
  return s === 3 ? 'success' : s === 4 ? 'danger' : s === 2 ? 'warning' : 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await contractApi.page({ ...query })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCustomers() {
  const res = await customerApi.list()
  customers.value = res.data || []
}

function openCreate() {
  Object.keys(create.form).forEach((k) => delete create.form[k])
  create.form.payType = 1
  create.periods = 3
  create.visible = true
}

async function submitCreate() {
  if (!create.form.customerId) return ElMessage.warning('请选择客户')
  if (!create.form.amount) return ElMessage.warning('请填写合同金额')
  create.loading = true
  try {
    await contractApi.create(create.form, create.form.payType === 2 ? create.periods : 1)
    ElMessage.success('合同已签订，回款计划已生成')
    create.visible = false
    loadData()
  } finally {
    create.loading = false
  }
}

async function openDetail(row) {
  detail.visible = true
  detail.loading = true
  try {
    const res = await contractApi.detail(row.id)
    detail.data = res.data
  } finally {
    detail.loading = false
  }
}

function openPay(row) {
  Object.keys(pay.form).forEach((k) => delete pay.form[k])
  pay.form.id = row.id
  pay.form.period = row.period
  pay.form.planAmount = row.planAmount
  pay.form.actualAmount = row.actualAmount || row.planAmount
  pay.form.actualDate = row.actualDate || new Date().toISOString().slice(0, 10)
  pay.visible = true
}

async function submitPay() {
  if (!pay.form.actualAmount) return ElMessage.warning('请填写实收金额')
  pay.loading = true
  try {
    await contractApi.registerPayment({
      id: pay.form.id,
      actualAmount: pay.form.actualAmount,
      actualDate: pay.form.actualDate,
      remark: pay.form.remark
    })
    ElMessage.success('回款已登记')
    pay.visible = false
    await openDetail({ id: detail.data.contract.id })
    loadData()
  } finally {
    pay.loading = false
  }
}

async function handleRemove(row) {
  await ElMessageBox.confirm(`确认删除合同 ${row.contractNo} 及其回款计划？`, '提示', { type: 'warning' })
  await contractApi.remove(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => {
  loadData()
  loadCustomers()
})
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  border-left: 3px solid #93c5fd;
  padding: 8px 12px;
  border-radius: 4px;
  margin-top: 4px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.detail-head h3 { font-size: 16px; font-weight: 600; }
.sub { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.metrics { display: flex; gap: 12px; margin-bottom: 20px; }
.metric {
  flex: 1;
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}
.metric b { display: block; font-size: 17px; font-weight: 600; color: #1e293b; }
.metric b.ok { color: #059669; }
.metric b.warn { color: #d97706; }
.metric span { font-size: 12px; color: #94a3b8; }
.section-title { font-size: 14px; font-weight: 600; margin-bottom: 10px; }
.paid { color: #059669; }
</style>

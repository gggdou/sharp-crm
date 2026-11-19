<template>
  <div class="page-card">
    <div class="page-header">
      <h2>待办中心</h2>
      <el-button :loading="loading" @click="loadData">刷新</el-button>
    </div>

    <div class="summary">
      <div class="sum-item overdue">
        <b>{{ summary.overdue }}</b>
        <span>已逾期</span>
      </div>
      <div class="sum-item today">
        <b>{{ summary.today }}</b>
        <span>今天到期</span>
      </div>
      <div class="sum-item soon">
        <b>{{ summary.soon }}</b>
        <span>三天内</span>
      </div>
      <div class="sum-item total">
        <b>{{ summary.total }}</b>
        <span>合计</span>
      </div>
    </div>

    <el-empty v-if="!items.length" description="暂无待办" />

    <div v-else class="todo-list" v-loading="loading">
      <div
        v-for="item in items"
        :key="item.type + '-' + item.id"
        class="todo-row"
        :class="'urgency-' + item.urgency"
      >
        <div class="bar"></div>
        <div class="main">
          <div class="row-head">
            <span class="type-tag">{{ typeName(item.type) }}</span>
            <b>{{ item.title || '未留姓名' }}</b>
            <span class="phone">{{ item.phone || '-' }}</span>
          </div>
          <div class="detail">{{ item.detail }}</div>
        </div>
        <div class="time">{{ item.dueTime || '-' }}</div>
        <el-button size="small" type="primary" plain @click="go(item)">{{ item.action || '去处理' }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { todoApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const items = ref([])
const summary = reactive({ overdue: 0, today: 0, soon: 0, total: 0 })

const TYPE = { business: '商机', clue: '线索', contract: '合同' }

function typeName(type) { return TYPE[type] || type }

async function loadData() {
  loading.value = true
  try {
    const res = await todoApi.list()
    const data = res.data || {}
    items.value = data.items || []
    Object.assign(summary, data.summary || {})
  } finally {
    loading.value = false
  }
}

function go(item) {
  router.push(item.link || '/dashboard')
}

onMounted(loadData)
</script>

<style scoped>
.summary { display: flex; gap: 12px; margin-bottom: 20px; }
.sum-item {
  flex: 1;
  border-radius: 10px;
  padding: 14px 16px;
  background: #f8fafc;
  border-left: 3px solid #cbd5e1;
}
.sum-item b { display: block; font-size: 24px; font-weight: 700; color: #1e293b; line-height: 1.2; }
.sum-item span { font-size: 12px; color: #94a3b8; }
.sum-item.overdue { border-left-color: #ef4444; }
.sum-item.overdue b { color: #dc2626; }
.sum-item.today { border-left-color: #f59e0b; }
.sum-item.today b { color: #d97706; }
.sum-item.soon { border-left-color: #3b82f6; }
.sum-item.soon b { color: #2563eb; }

.todo-list { display: flex; flex-direction: column; gap: 10px; }
.todo-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px 14px 0;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  transition: border-color .15s;
}
.todo-row:hover { border-color: #93c5fd; }
.bar { width: 3px; align-self: stretch; border-radius: 0 3px 3px 0; background: #cbd5e1; }
.urgency-0 .bar { background: #ef4444; }
.urgency-1 .bar { background: #f59e0b; }
.urgency-2 .bar { background: #3b82f6; }

.main { flex: 1; min-width: 0; }
.row-head { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.row-head b { font-size: 14px; font-weight: 600; }
.phone { font-size: 12px; color: #94a3b8; }
.type-tag {
  font-size: 11px; padding: 1px 6px; border-radius: 4px;
  background: #eef2ff; color: #4338ca;
}
.detail { font-size: 13px; color: #64748b; margin-top: 5px; }
.urgency-0 .detail { color: #dc2626; }
.time { font-size: 12px; color: #94a3b8; white-space: nowrap; }
</style>

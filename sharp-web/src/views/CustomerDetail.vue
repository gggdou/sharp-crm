<template>
  <div v-loading="loading">
    <div class="page-card hero">
      <div class="hero-left">
        <div class="avatar">{{ (customer.name || '?').slice(0, 1) }}</div>
        <div class="hero-title">
          <h2>
            {{ customer.name || '未知客户' }}
            <el-tag type="success" size="small" effect="plain">已成交</el-tag>
          </h2>
          <p class="hero-sub">
            <span>{{ customer.phone || '无手机号' }}</span>
            <span v-if="subjectName">· {{ subjectName }}</span>
            <span v-if="channelName">· {{ channelName }}</span>
          </p>
        </div>
      </div>
      <div class="hero-metrics">
        <div class="metric">
          <b>{{ stat.convertDays == null ? '—' : stat.convertDays }}</b>
          <span>转化天数</span>
        </div>
        <div class="metric">
          <b>{{ stat.followCount || 0 }}</b>
          <span>累计跟进</span>
        </div>
        <div class="metric">
          <b class="metric-text">{{ ownerName || '未分配' }}</b>
          <span>归属人</span>
        </div>
      </div>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <div class="page-card">
          <div class="card-head">
            <h3>客户轨迹</h3>
            <span class="hint">线索 {{ stat.clueFollowCount || 0 }} 次跟进 · 商机 {{ stat.businessFollowCount || 0 }} 次跟进</span>
          </div>
          <el-empty v-if="!timeline.length" description="暂无跟进记录" />
          <el-timeline v-else class="timeline">
            <el-timeline-item
              v-for="(e, i) in timeline"
              :key="i"
              :timestamp="e.time || '时间未知'"
              :color="toneColor(e.tone)"
              placement="top"
            >
              <div class="event">
                <div class="event-head">
                  <b>{{ e.title }}</b>
                  <el-tag v-if="e.tag" size="small" :type="tagType(e.tone)" effect="light">{{ e.tag }}</el-tag>
                  <span class="stage-dot" :class="'stage-' + e.stage">{{ stageName(e.stage) }}</span>
                </div>
                <p v-if="e.content" class="event-content">{{ e.content }}</p>
                <span v-if="e.operator" class="event-meta">操作人：{{ e.operator }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="page-card">
          <div class="card-head"><h3>来源链路</h3></div>
          <div v-if="!sourceClue && !sourceBusiness" class="empty-hint">
            该客户由历史数据导入，没有可追溯的线索或商机记录
          </div>
          <div v-else class="chain">
            <div v-if="sourceClue" class="chain-node">
              <div class="chain-label">线索</div>
              <div class="chain-main">
                <b>#{{ sourceClue.id }} {{ sourceClue.name || '未留姓名' }}</b>
                <span>{{ sourceClue.phone || '-' }}</span>
              </div>
              <div class="chain-foot">进入时间 {{ sourceClue.createTime || '-' }}</div>
            </div>
            <div v-if="sourceClue && sourceBusiness" class="chain-arrow">↓</div>
            <div v-if="sourceBusiness" class="chain-node">
              <div class="chain-label">商机</div>
              <div class="chain-main">
                <b>#{{ sourceBusiness.id }} {{ sourceBusiness.name || '未留姓名' }}</b>
                <span>{{ sourceBusiness.phone || '-' }}</span>
              </div>
              <div class="chain-foot">创建时间 {{ sourceBusiness.createTime || '-' }}</div>
            </div>
            <div class="chain-arrow">↓</div>
            <div class="chain-node current">
              <div class="chain-label">客户</div>
              <div class="chain-main">
                <b>#{{ customer.id }} {{ customer.name || '未留姓名' }}</b>
                <span>成交</span>
              </div>
              <div class="chain-foot">成交时间 {{ customer.createTime || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="page-card" style="margin-top: 16px">
          <div class="card-head">
            <h3>合同与回款</h3>
            <span class="hint" v-if="contracts.length">共 {{ contracts.length }} 份</span>
          </div>
          <div v-if="!contracts.length" class="empty-hint">该客户还没有签约记录</div>
          <template v-else>
            <div v-for="c in contracts" :key="c.id" class="contract-row" @click="router.push('/contract')">
              <div class="c-left">
                <b>{{ c.contractNo }}</b>
                <span>{{ c.payType === 2 ? '分期' : '一次性' }} · {{ c.signDate || '-' }}</span>
              </div>
              <div class="c-right">
                <b>¥{{ money(c.amount) }}</b>
                <span>{{ CONTRACT_STATUS[c.status] || '-' }}</span>
              </div>
            </div>
            <div class="contract-sum">
              合同总额 ¥{{ money(stat.contractAmount) }} · 已回款 ¥{{ money(stat.paidAmount) }}
              <em>（{{ stat.receivableRate || 0 }}%）</em>
            </div>
          </template>
        </div>

        <div class="page-card" style="margin-top: 16px">
          <div class="card-head"><h3>客户档案</h3></div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="性别">{{ genderName }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ customer.age || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学历">{{ degreeName }}</el-descriptions-item>
            <el-descriptions-item label="微信号">{{ customer.wechat || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ customer.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { customerApi } from '@/api'

const route = useRoute()
const router = useRouter()

const SUBJECT = { 1:'AI 应用开发', 2:'AI 大模型开发', 3:'AI 鸿蒙开发', 4:'AI 大数据', 5:'AI 嵌入式', 6:'AI 测试', 7:'AI 运维' }
const CHANNEL = { 1:'线上活动', 2:'推广介绍' }
const DEGREE = { 1:'高中', 2:'中专', 3:'大专', 4:'本科', 5:'硕士', 6:'博士', 7:'其他' }
const GENDER = { 1:'男', 2:'女' }
const STAGE = { clue:'线索', business:'商机', customer:'客户', contract:'合同' }
const CONTRACT_STATUS = { 1:'草稿', 2:'生效中', 3:'已完成', 4:'已作废' }

const loading = ref(false)
const customer = reactive({})
const stat = reactive({})
const sourceClue = ref(null)
const sourceBusiness = ref(null)
const ownerName = ref('')
const timeline = ref([])
const contracts = ref([])

function money(v) { return Number(v || 0).toFixed(2) }

const subjectName = computed(() => SUBJECT[customer.subject])
const channelName = computed(() => CHANNEL[customer.channel])
const genderName = computed(() => GENDER[customer.gender] || '-')
const degreeName = computed(() => DEGREE[customer.degree] || '-')

function stageName(stage) { return STAGE[stage] || stage }
function toneColor(tone) { return tone === -1 ? '#ef4444' : tone === 1 ? '#2563eb' : '#cbd5e1' }
function tagType(tone) { return tone === -1 ? 'danger' : tone === 1 ? 'primary' : 'info' }

async function loadData() {
  loading.value = true
  try {
    const res = await customerApi.find360(route.params.id)
    const data = res.data || {}
    Object.keys(customer).forEach((k) => delete customer[k])
    Object.assign(customer, data.customer || {})
    Object.keys(stat).forEach((k) => delete stat[k])
    Object.assign(stat, data.stat || {})
    sourceClue.value = data.sourceClue || null
    sourceBusiness.value = data.sourceBusiness || null
    ownerName.value = data.ownerName || ''
    timeline.value = data.timeline || []
    contracts.value = data.contracts || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}
.hero-left { display: flex; align-items: center; gap: 16px; }
.avatar {
  width: 56px; height: 56px; border-radius: 14px;
  background: #dbeafe; color: #1d4ed8;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 600;
}
.hero-title h2 { font-size: 20px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.hero-sub { color: #64748b; font-size: 13px; margin-top: 6px; }
.hero-sub span { margin-right: 6px; }
.hero-metrics { display: flex; gap: 32px; }
.metric { text-align: center; }
.metric b { display: block; font-size: 24px; font-weight: 700; color: #1d4ed8; line-height: 1.2; }
.metric b.metric-text { font-size: 18px; }
.metric span { font-size: 12px; color: #94a3b8; }

.card-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16px; }
.card-head h3 { font-size: 15px; font-weight: 600; }
.hint { font-size: 12px; color: #94a3b8; }

.timeline { padding-left: 4px; }
.event-head { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.event-head b { font-size: 14px; font-weight: 600; }
.event-content { color: #475569; font-size: 13px; margin-top: 6px; line-height: 1.6; }
.event-meta { display: inline-block; margin-top: 6px; font-size: 12px; color: #94a3b8; }
.stage-dot {
  font-size: 11px; padding: 1px 6px; border-radius: 4px;
  background: #f1f5f9; color: #64748b;
}
.stage-clue { background: #ecfeff; color: #0e7490; }
.stage-business { background: #eef2ff; color: #4338ca; }
.stage-customer { background: #ecfdf5; color: #047857; }
.stage-contract { background: #fef3c7; color: #b45309; }

.contract-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
.contract-row:hover { border-color: #93c5fd; }
.c-left b { display: block; font-size: 13px; font-weight: 600; }
.c-left span { font-size: 11px; color: #94a3b8; }
.c-right { text-align: right; }
.c-right b { display: block; font-size: 14px; font-weight: 600; color: #1e293b; }
.c-right span { font-size: 11px; color: #64748b; }
.contract-sum {
  margin-top: 10px; padding-top: 10px; border-top: 1px dashed #e2e8f0;
  font-size: 12px; color: #64748b;
}
.contract-sum em { font-style: normal; color: #059669; font-weight: 600; }

.chain-node {
  border: 1px solid #e2e8f0; border-radius: 10px;
  padding: 12px 14px; position: relative;
}
.chain-node.current { border-color: #86efac; background: #f0fdf4; }
.chain-label {
  position: absolute; top: -9px; left: 12px;
  font-size: 11px; background: #fff; padding: 0 6px; color: #94a3b8;
}
.chain-main { display: flex; justify-content: space-between; align-items: baseline; gap: 8px; }
.chain-main b { font-size: 13px; font-weight: 600; }
.chain-main span { font-size: 12px; color: #64748b; }
.chain-foot { font-size: 11px; color: #94a3b8; margin-top: 6px; }
.chain-arrow { text-align: center; color: #cbd5e1; line-height: 22px; font-size: 14px; }
.empty-hint { color: #94a3b8; font-size: 13px; line-height: 1.7; }
</style>

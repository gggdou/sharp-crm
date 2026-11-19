<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6" v-for="s in cards" :key="s.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: s.color }">
            <el-icon :size="22" color="#fff"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card todo-strip" @click="router.push('/todo')">
      <div class="strip-left">
        <span class="strip-dot" :class="{ active: todo.overdue > 0 }"></span>
        <b>今日待办</b>
        <span class="strip-sub">
          逾期 <em>{{ todo.overdue }}</em> · 今天 <em>{{ todo.today }}</em> · 三天内 <em>{{ todo.soon }}</em>
        </span>
      </div>
      <span class="strip-link">{{ todo.total ? '查看全部 →' : '暂无待办' }}</span>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="16">
        <div class="page-card">
          <div class="card-head">
            <h3>近 6 个月招生转化趋势</h3>
            <span class="hint">线索转商机 {{ rate.clueToBusiness }}% · 商机成交 {{ rate.businessToCustomer }}%</span>
          </div>
          <div ref="lineRef" style="height: 300px"></div>
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="page-card">
          <div class="card-head"><h3>线索来源分布</h3></div>
          <div ref="pieRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="12">
        <div class="page-card">
          <div class="card-head"><h3>转化漏斗</h3></div>
          <div ref="funnelRef" style="height: 260px"></div>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="page-card">
          <div class="card-head"><h3>线索状态分布</h3></div>
          <div ref="barRef" style="height: 260px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
// 按需引入：只注册本项目真正用到的图表与组件，避免把整包 ECharts 打进产物
import * as echarts from 'echarts/core'
import { LineChart, PieChart, FunnelChart, BarChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent,
  GridComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { dashboardApi, todoApi } from '@/api'

echarts.use([
  LineChart, PieChart, FunnelChart, BarChart,
  TooltipComponent, LegendComponent, GridComponent, TitleComponent,
  CanvasRenderer
])

const router = useRouter()
const loading = ref(false)
const lineRef = ref()
const pieRef = ref()
const funnelRef = ref()
const barRef = ref()

const charts = []
const rate = ref({ clueToBusiness: 0, businessToCustomer: 0 })
const todo = ref({ overdue: 0, today: 0, soon: 0, total: 0 })
const cards = ref([
  { label: '线索总数', value: 0, icon: 'Tickets', color: '#2563eb' },
  { label: '商机总数', value: 0, icon: 'TrendCharts', color: '#7c3aed' },
  { label: '客户总数', value: 0, icon: 'UserFilled', color: '#059669' },
  { label: '本月新增线索', value: 0, icon: 'Calendar', color: '#d97706' }
])

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, todoRes] = await Promise.all([dashboardApi.stats(), todoApi.list()])
    render(statsRes.data)
    todo.value = todoRes.data?.summary || { overdue: 0, today: 0, soon: 0, total: 0 }
  } catch (e) {
    // 接口异常时保留空态，不阻塞页面
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  charts.forEach((c) => c.dispose())
})

function render(d) {
  if (!d) return
  const o = d.overview || {}
  cards.value[0].value = o.clueTotal ?? 0
  cards.value[1].value = o.businessTotal ?? 0
  cards.value[2].value = o.customerTotal ?? 0
  cards.value[3].value = o.clueThisMonth ?? 0
  rate.value = {
    clueToBusiness: o.clueToBusinessRate ?? 0,
    businessToCustomer: o.businessToCustomerRate ?? 0
  }

  const trend = d.trend || []
  renderLine(trend)
  renderPie(d.channelDist || [])
  renderFunnel(d.funnel || [])
  renderBar(d.statusDist || [])
}

function initChart(el) {
  const chart = echarts.init(el)
  charts.push(chart)
  return chart
}

function monthLabel(month) {
  // 2026-08 → 8月
  return month ? `${Number(month.slice(5))}月` : ''
}

function renderLine(trend) {
  const chart = initChart(lineRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增线索', '转化商机'], right: 0 },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', boundaryGap: false, data: trend.map((t) => monthLabel(t.month)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新增线索',
        type: 'line',
        smooth: true,
        data: trend.map((t) => t.clueCount),
        areaStyle: { color: 'rgba(37,99,235,.12)' },
        itemStyle: { color: '#2563eb' }
      },
      {
        name: '转化商机',
        type: 'line',
        smooth: true,
        data: trend.map((t) => t.businessCount),
        itemStyle: { color: '#7c3aed' }
      }
    ]
  })
}

function renderPie(channelDist) {
  const chart = initChart(pieRef.value)
  const total = channelDist.reduce((sum, i) => sum + i.value, 0)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    title: {
      text: String(total),
      left: 'center',
      top: '37%',
      textAlign: 'center',
      textStyle: { fontSize: 24, fontWeight: 700, color: '#0f172a' }
    },
    legend: {
      bottom: 0,
      // 图例直接带数量，窄屏下标签不会挤成省略号
      formatter: (name) => {
        const hit = channelDist.find((i) => i.name === name)
        return hit ? `${name} ${hit.value}` : name
      }
    },
    color: ['#2563eb', '#38bdf8', '#a78bfa', '#94a3b8'],
    series: [
      {
        type: 'pie',
        radius: ['48%', '70%'],
        center: ['50%', '44%'],
        label: { show: false },
        data: channelDist.map((i) => ({ name: i.name, value: i.value }))
      }
    ]
  })
}

function renderFunnel(funnel) {
  const chart = initChart(funnelRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    color: ['#2563eb', '#7c3aed', '#059669'],
    series: [
      {
        type: 'funnel',
        left: '10%',
        right: '10%',
        top: 20,
        bottom: 20,
        minSize: '30%',
        gap: 4,
        // 按 线索→商机→客户 的业务阶段顺序固定排列，不按数值重排
        sort: 'none',
        label: { position: 'inside', formatter: '{b} {c}' },
        data: funnel.map((f) => ({ name: f.name, value: f.value }))
      }
    ]
  })
}

function renderBar(statusDist) {
  const chart = initChart(barRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 70, right: 30, top: 20, bottom: 20 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: statusDist.map((i) => i.name) },
    color: ['#38bdf8'],
    series: [
      {
        type: 'bar',
        barWidth: 18,
        itemStyle: { borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right' },
        data: statusDist.map((i) => i.value)
      }
    ]
  })
}
</script>

<style scoped>
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex: none; }
.card-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16px; }
.card-head h3 { margin: 0; }
.hint { font-size: 12px; color: var(--el-text-color-secondary); }
.todo-strip {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  cursor: pointer;
  transition: box-shadow .15s;
}
.todo-strip:hover { box-shadow: 0 2px 10px rgba(37, 99, 235, .12); }
.strip-left { display: flex; align-items: center; gap: 10px; }
.strip-left b { font-size: 14px; font-weight: 600; }
.strip-dot { width: 7px; height: 7px; border-radius: 50%; background: #cbd5e1; }
.strip-dot.active { background: #ef4444; box-shadow: 0 0 0 3px rgba(239, 68, 68, .15); }
.strip-sub { font-size: 13px; color: #64748b; }
.strip-sub em { font-style: normal; font-weight: 600; color: #1e293b; }
.strip-link { font-size: 13px; color: var(--zk-primary); }
</style>

<template>
  <SimpleCrud title="市场活动" :api="activityApi" :columns="columns" :searchFields="searchFields" :editFields="editFields" />
</template>
<script setup>
import SimpleCrud from '@/components/SimpleCrud.vue'
import { activityApi } from '@/api'
const channelMap = {1:'线上活动',2:'推广介绍'}
const channelOpts = Object.entries(channelMap).map(([value,label])=>({value:Number(value),label}))
const columns = [
  { prop:'id', label:'ID', width:80 },
  { prop:'name', label:'活动名称' },
  { prop:'channel', label:'渠道', format:v=>channelMap[v]||'-' },
  { prop:'type', label:'类型' },
  { prop:'discount', label:'折扣' },
  { prop:'voucher', label:'代金券' },
  { prop:'startTime', label:'开始时间', width:170 },
  { prop:'endTime', label:'结束时间', width:170 }
]
const searchFields = [{ prop:'channel', label:'渠道', type:'select', options:channelOpts }]
const editFields = [
  { prop:'name', label:'活动名称' },
  { prop:'channel', label:'渠道', type:'select', options:channelOpts },
  { prop:'type', label:'类型', type:'number' },
  { prop:'startTime', label:'开始时间', type:'datetime' },
  { prop:'endTime', label:'结束时间', type:'datetime' },
  { prop:'discount', label:'折扣' },
  { prop:'voucher', label:'代金券', type:'number' },
  { prop:'description', label:'活动说明', type:'textarea' }
]
</script>

<template>
  <SimpleCrud title="部门" admin-only :api="deptApi" :columns="columns" :searchFields="searchFields" :editFields="editFields" />
</template>
<script setup>
import SimpleCrud from '@/components/SimpleCrud.vue'
import { deptApi } from '@/api'
const statusMap = {0:'停用',1:'正常'}
const statusOpts = Object.entries(statusMap).map(([value,label])=>({value:Number(value),label}))
const columns = [
  { prop:'id', label:'ID', width:80 },
  { prop:'name', label:'部门名称' },
  { prop:'status', label:'状态', width:100, format:v=>statusMap[v]||'-' },
  { prop:'createTime', label:'创建时间', width:170 }
]
const searchFields = [{ prop:'name', label:'部门名称' }]
const editFields = [
  { prop:'name', label:'部门名称' },
  { prop:'status', label:'状态', type:'select', options:statusOpts }
]
</script>

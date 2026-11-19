<template>
  <SimpleCrud title="用户" admin-only :api="userApi" :columns="columns" :searchFields="searchFields" :editFields="editFields" />
</template>
<script setup>
import SimpleCrud from '@/components/SimpleCrud.vue'
import { userApi } from '@/api'
const statusMap = {0:'停用',1:'正常'}
const statusOpts = Object.entries(statusMap).map(([value,label])=>({value:Number(value),label}))
const columns = [
  { prop:'id', label:'ID', width:70 },
  { prop:'username', label:'用户名' },
  { prop:'name', label:'姓名' },
  { prop:'phone', label:'手机号', width:130 },
  { prop:'email', label:'邮箱', width:180 },
  { prop:'deptName', label:'部门' },
  { prop:'roleName', label:'角色' },
  { prop:'status', label:'状态', width:90, format:v=>statusMap[v]||'-' }
]
const searchFields = [
  { prop:'name', label:'姓名' },
  { prop:'phone', label:'手机号' },
  { prop:'status', label:'状态', type:'select', options:statusOpts }
]
const editFields = [
  { prop:'username', label:'用户名' },
  { prop:'name', label:'姓名' },
  { prop:'phone', label:'手机号' },
  { prop:'email', label:'邮箱' },
  { prop:'gender', label:'性别', type:'select', options:[{value:1,label:'男'},{value:2,label:'女'}] },
  { prop:'deptId', label:'部门ID', type:'number' },
  { prop:'roleId', label:'角色ID', type:'number' },
  { prop:'status', label:'状态', type:'select', options:statusOpts }
]
</script>

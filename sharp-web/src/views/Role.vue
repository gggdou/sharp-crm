<template>
  <SimpleCrud title="角色" admin-only :api="roleApi" :columns="columns" :searchFields="searchFields" :editFields="editFields" />
</template>
<script setup>
import SimpleCrud from '@/components/SimpleCrud.vue'
import { roleApi } from '@/api'
const columns = [
  { prop:'id', label:'ID', width:80 },
  { prop:'name', label:'角色名称' },
  { prop:'label', label:'角色标识' },
  { prop:'remark', label:'备注' },
  { prop:'createTime', label:'创建时间', width:170 }
]
const searchFields = [{ prop:'name', label:'角色名称' }]
const editFields = [
  { prop:'name', label:'角色名称' },
  { prop:'label', label:'角色标识' },
  { prop:'remark', label:'备注' }
]
</script>

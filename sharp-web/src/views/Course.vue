<template>
  <SimpleCrud title="课程" :api="courseApi" :columns="columns" :searchFields="searchFields" :editFields="editFields" />
</template>
<script setup>
import SimpleCrud from '@/components/SimpleCrud.vue'
import { courseApi } from '@/api'
const subjectMap = {1:'AI应用开发(Java)',2:'AI大模型(Python)',3:'AI鸿蒙',4:'AI大数据',5:'AI嵌入式',6:'AI测试',7:'AI运维'}
const targetMap = {1:'小白学员',2:'初级程序员',3:'中级程序员'}
const subjectOpts = Object.entries(subjectMap).map(([value,label])=>({value:Number(value),label}))
const targetOpts = Object.entries(targetMap).map(([value,label])=>({value:Number(value),label}))
const columns = [
  { prop:'id', label:'ID', width:80 },
  { prop:'name', label:'课程名称' },
  { prop:'subject', label:'学科', format:v=>subjectMap[v]||'-' },
  { prop:'price', label:'价格(元)', width:110 },
  { prop:'target', label:'适用人群', format:v=>targetMap[v]||'-' },
  { prop:'description', label:'介绍' }
]
const searchFields = [
  { prop:'name', label:'课程名称' },
  { prop:'subject', label:'学科', type:'select', options:subjectOpts }
]
const editFields = [
  { prop:'name', label:'课程名称' },
  { prop:'subject', label:'学科', type:'select', options:subjectOpts },
  { prop:'price', label:'价格', type:'number' },
  { prop:'target', label:'适用人群', type:'select', options:targetOpts },
  { prop:'description', label:'介绍' }
]
</script>

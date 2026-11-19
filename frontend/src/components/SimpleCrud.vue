<template>
  <div class="page-card">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-button v-if="api.add" type="primary" v-permission="adminOnly ? ['admin'] : null" @click="openAdd">新增</el-button>
    </div>

    <el-form :inline="true" :model="query">
      <el-form-item v-for="f in searchFields" :key="f.prop" :label="f.label">
        <el-select v-if="f.type === 'select'" v-model="query[f.prop]" :placeholder="'全部'" clearable style="width:130px">
          <el-option v-for="opt in f.options" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-input v-else v-model="query[f.prop]" :placeholder="f.label" clearable />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label" :width="c.width">
        <template v-if="c.format" #default="{ row }">{{ c.format(row[c.prop]) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" v-if="api.update || api.remove">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)" v-if="api.update" v-permission="adminOnly ? ['admin'] : null">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-if="api.remove" v-permission="adminOnly ? ['admin'] : null">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next"
                   :total="total" v-model:current-page="query.page" v-model:page-size="query.pageSize" @current-change="loadData" />

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item v-for="f in editFields" :key="f.prop" :label="f.label" :prop="f.required ? f.prop : undefined">
          <el-select v-if="f.type === 'select'" v-model="form[f.prop]" style="width:100%">
            <el-option v-for="opt in f.options" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-date-picker v-else-if="f.type === 'datetime'" v-model="form[f.prop]" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" style="width:100%" />
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" style="width:100%" />
          <el-input v-else-if="f.type === 'textarea'" v-model="form[f.prop]" type="textarea" :rows="2" />
          <el-input v-else v-model="form[f.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible=false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  title: String,
  api: Object,
  columns: Array,
  searchFields: { type: Array, default: () => [] },
  editFields: { type: Array, default: () => [] },
  // 该页面的写操作是否仅管理员可见（后端对应接口已标 @RequireRole("admin")）
  adminOnly: { type: Boolean, default: false }
})

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page:1, pageSize:10 })
const dialog = reactive({ visible:false, title:'' })
const form = reactive({})
const formRef = ref(null)

// 依据 editFields 中标记 required 的字段生成校验规则
const formRules = computed(() => {
  const rules = {}
  props.editFields.forEach(f => {
    if (f.required) rules[f.prop] = [{ required: true, message: `请输入${f.label}`, trigger: 'blur' }]
  })
  return rules
})

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    props.searchFields.forEach(f => { delete params[f.prop] })
    props.searchFields.forEach(f => { if (query[f.prop] !== '' && query[f.prop] != null) params[f.prop] = query[f.prop] })
    const res = await props.api.page(params)
    list.value = res.data?.rows || []; total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openAdd() {
  Object.keys(form).forEach(k => delete form[k]); dialog.title = '新增' + props.title; dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}
function openEdit(row) {
  Object.keys(form).forEach(k => delete form[k]); Object.assign(form, row)
  dialog.title = '编辑' + props.title; dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}
async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) await props.api.update(form); else await props.api.add(form)
  ElMessage.success('保存成功'); dialog.visible = false; loadData()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示')
  await props.api.remove(row.id); ElMessage.success('删除成功'); loadData()
}
onMounted(loadData)
</script>

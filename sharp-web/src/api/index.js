import request from '@/utils/request'

// 登录 / 注册 / 验证码
export const login = (data) => request.post('/login', data)
export const register = (data) => request.post('/register', data)
export const getCaptcha = () => request.get('/captcha')

// 通用 CRUD 工厂：生成某资源的 restful 方法
const crud = (base) => ({
  page: (params) => request.get(base, { params }),
  list: () => request.get(`${base}/list`),
  detail: (id) => request.get(`${base}/${id}`),
  add: (data) => request.post(base, data),
  update: (data) => request.put(base, data),
  remove: (id) => request.delete(`${base}/${id}`)
})

export const userApi = {
  ...crud('/users'),
  byRole: (label) => request.get(`/users/role/${label}`),
  byDept: (id) => request.get(`/users/dept/${id}`),
  batchRemove: (ids) => request.delete(`/users/${ids.join(',')}`)
}

export const clueApi = {
  ...crud('/clues'),
  pool: (params) => request.get('/clues/pool', { params }),
  assign: (clueId, userId) => request.put(`/clues/assign/${clueId}/${userId}`),
  follow: (data) => request.put('/clues', data),
  falseFollow: (id, data) => request.put(`/clues/false/${id}`, data),
  toBusiness: (id) => request.put(`/clues/toBusiness/${id}`),
  import: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/clues/import', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  exportUrl: '/api/clues/export'
}

export const businessApi = {
  ...crud('/businesses'),
  pool: (params) => request.get('/businesses/pool', { params }),
  assign: (id, userId) => request.put(`/businesses/assign/${id}/${userId}`),
  back: (id) => request.put(`/businesses/back/${id}`),
  follow: (data) => request.put('/businesses', data),
  toCustomer: (id) => request.put(`/businesses/toCustomer/${id}`)
}

export const courseApi = crud('/courses')
export const activityApi = { ...crud('/activities'), byType: (type) => request.get(`/activities/type/${type}`) }
export const deptApi = crud('/depts')
export const roleApi = crud('/roles')
export const customerApi = {
  ...crud('/customers'),
  find360: (id) => request.get(`/customers/${id}/360`)
}
export const logApi = { page: (params) => request.get('/logs', { params }) }
export const dashboardApi = { stats: () => request.get('/dashboard/stats') }
export const todoApi = { list: () => request.get('/dashboard/todos') }

export const contractApi = {
  page: (params) => request.get('/contracts', { params }),
  detail: (id) => request.get(`/contracts/${id}`),
  create: (data, periods) => request.post('/contracts', data, { params: { periods } }),
  update: (data) => request.put('/contracts', data),
  remove: (id) => request.delete(`/contracts/${id}`),
  registerPayment: (data) => request.put('/contracts/payment', data)
}

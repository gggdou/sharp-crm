/**
 * 按钮级权限指令
 * <p>
 * 用法：
 *   v-permission="'admin'"                   仅管理员可见
 *   v-permission="['admin', 'clue_operator']" 任一角色可见
 * <p>
 * 说明：这只是体验层收敛，真正的鉴权在后端 @RequireRole 切面。
 * 前端隐藏按钮从不作为安全边界——直接构造请求依然会被服务端拦下。
 */
import { useUserStore } from '@/store/user'

const ADMIN_ONLY = ['admin']

function allowed(value) {
  if (!value) return true
  const roleLabel = useUserStore().userInfo?.roleLabel
  const list = Array.isArray(value) ? value : [value]
  return list.includes(roleLabel)
}

export default {
  mounted(el, binding) {
    if (!allowed(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export { ADMIN_ONLY }

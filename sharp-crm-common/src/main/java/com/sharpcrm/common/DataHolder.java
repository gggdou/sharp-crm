package com.sharpcrm.common;

import org.springframework.stereotype.Component;

@Component
public class DataHolder<T> {
    private ThreadLocal<T> holder = new ThreadLocal<T>();


    public void set(T value) { // 设置当前线程的值
        holder.set(value);
    }

    public T get() { // 获取当前线程的值
        return holder.get();
    }

    public void remove() { // 移除当前线程的值
        holder.remove();
    }

    /**
     * 取当前登录用户 id（载荷中的 id），上下文为空或格式异常时返回 null
     */
    public Integer getCurrentUserId() {
        Object value = get();
        if (value instanceof java.util.Map<?, ?> claims) {
            Object id = claims.get("id");
            if (id != null) {
                try {
                    return Integer.parseInt(id.toString());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 取当前登录用户的角色标识（载荷中的 roleLabel），未登录或旧令牌返回 null
     */
    public String getCurrentRoleLabel() {
        Object value = get();
        if (value instanceof java.util.Map<?, ?> claims) {
            Object roleLabel = claims.get("roleLabel");
            return roleLabel == null ? null : roleLabel.toString();
        }
        return null;
    }

    /**
     * 当前登录用户是否为管理员：管理员可见全部数据、可执行管理类操作
     */
    public boolean isAdmin() {
        return "admin".equals(getCurrentRoleLabel());
    }
}

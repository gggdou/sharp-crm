package cn.itcast.common;

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
}

package cn.itcast.config;

import cn.itcast.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：注册登录拦截器，配置拦截和放行路径
 */
@Component
public class WebConfig implements WebMvcConfigurer {

    // 注入登录拦截器
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 添加拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/login","/register"); // 放行登录、注册接口，不需要token校验

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}

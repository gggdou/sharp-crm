package com.sharpcrm.config;

import com.sharpcrm.interceptor.LoginInterceptor;
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
                // 放行：登录、注册、验证码，接口文档静态资源，以及健康检查探针
                .excludePathPatterns(
                        "/login", "/register", "/captcha",
                        "/doc.html", "/webjars/**", "/v3/api-docs/**",
                        "/swagger-ui/**", "/swagger-resources/**", "/favicon.ico",
                        "/actuator/health", "/actuator/health/**", "/actuator/info");

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}

package cn.itcast.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.itcast.common.DataHolder;
import cn.itcast.common.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 登录拦截器：校验请求头中的JWT令牌，实现登录状态校验
 */
@Component // 将拦截器交给Spring IOC容器管理
public class LoginInterceptor implements HandlerInterceptor {
    // 注入DataHolder：基于ThreadLocal，存放当前登录用户信息，同一线程内随时获取
    @Autowired
    private DataHolder dataHolder;

    // 注入JWT工具类，用于解析token
    @Autowired
    private JWTUtils jwtUtils;

    /**
     * preHandle：在Controller方法执行之前执行
     * 返回true：放行；返回false：拦截，不进入Controller
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取token，约定前端把令牌放在header的token字段
        String token = request.getHeader("token");

        // 判断token为空或空白字符串
        if (StrUtil.isBlank(token)){
            response.setStatus(401); // 设置响应状态码401：未登录
            return false; // 拦截请求
        }

        try{
            // 解析token，拿到载荷（用户信息）
            Map<String, Object> map = jwtUtils.parseToken(token);
            // 将用户信息存入ThreadLocal，供Controller、Service层使用
            dataHolder.set(map);
        } catch (Exception e){
            // token解析异常：过期、篡改、格式错误，返回401未登录
            response.setStatus(401);
            return false;
        }

        // 校验全部通过，放行请求
        return true;
    }

    /**
     * afterCompletion：请求完全结束之后执行（视图渲染完成）
     * 只要preHandle放行成功，本方法一定会执行
     * 作用：清理ThreadLocal，防止内存泄漏，避免线程复用导致数据错乱
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除当前线程保存的用户数据
        dataHolder.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

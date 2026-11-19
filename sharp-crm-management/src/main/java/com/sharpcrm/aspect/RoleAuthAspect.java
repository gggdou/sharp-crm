package com.sharpcrm.aspect;

import com.sharpcrm.common.DataHolder;
import com.sharpcrm.common.annotation.RequireRole;
import com.sharpcrm.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 角色鉴权切面
 * <p>
 * 与登录拦截器的分工：拦截器只判断“有没有登录”，本切面判断“这个角色能不能干这件事”。
 * 角色标识在登录时写入 JWT 载荷，这里直接读取，无需再查库。
 */
@Slf4j
@Aspect
@Component
public class RoleAuthAspect {

    @Autowired
    private DataHolder<?> dataHolder;

    /**
     * 拦截标注了 @RequireRole 的方法，命中任一允许角色则放行，否则抛业务异常
     */
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String currentRole = dataHolder.getCurrentRoleLabel();

        // 载荷中没有角色，通常是改造前签发的旧令牌，需要重新登录才能拿到角色信息
        if (currentRole == null) {
            throw new CustomException("登录信息已过期，请重新登录");
        }

        boolean allowed = Arrays.asList(requireRole.value()).contains(currentRole);
        if (!allowed) {
            log.warn("越权访问被拦截，当前角色：{}，允许角色：{}", currentRole, Arrays.toString(requireRole.value()));
            throw new CustomException("当前角色无权执行该操作");
        }

        return joinPoint.proceed();
    }
}

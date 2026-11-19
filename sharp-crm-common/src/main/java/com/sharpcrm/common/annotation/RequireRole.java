package com.sharpcrm.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解，标注在 Controller 方法上，限定可调用该接口的角色标识
 * <p>
 * 角色标识取自 role 表的 label 字段，如 admin、clue_operator、business_operator。
 * 由 RoleAuthAspect 在方法执行前统一校验，未标注的接口仅要求登录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 允许访问的角色标识，满足其中任意一个即放行
     */
    String[] value();
}

package com.sharpcrm.validation;

/**
 * 校验分组：新增场景
 * <p>
 * 新增时必填的字段（如手机号、姓名）在更新时可能不传（只更新部分字段），
 * 用分组可以避免同一份实体上「新增要求必填、更新却报错」的矛盾。
 */
public interface Create {
}

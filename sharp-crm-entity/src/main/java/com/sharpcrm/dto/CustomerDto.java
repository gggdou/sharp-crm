package com.sharpcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户查询条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String name;
    private String phone;
    private Integer subject;
    private Integer userId;
    private Integer page = 1;
    private Integer pageSize = 10;

    // 数据权限范围，服务端根据当前登录角色推导后覆盖写入，前端传值无效（管理员为 null 表示不限）
    private Integer scopeUserId;
}

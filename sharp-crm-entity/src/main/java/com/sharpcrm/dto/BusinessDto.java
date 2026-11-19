package com.sharpcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {
    private Integer businessId;
    private String name;
    private String phone;
    private Integer status;
    private String assignName;
    private Integer page = 1;
    private Integer pageSize = 10;

    private Integer subject;

    // 数据权限范围，服务端根据当前登录角色推导后覆盖写入，前端传值无效（管理员为 null 表示不限）
    private Integer scopeUserId;
}

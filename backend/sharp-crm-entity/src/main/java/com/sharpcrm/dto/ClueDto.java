package com.sharpcrm.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClueDto {
        // 分页查询参数
        private Integer clueId;
        private String phone;
        private Integer status;
        private Integer channel;
        private String assignName;
        private Integer page = 1;
        private Integer pageSize = 10;

        // 伪线索的特定参数
        private Integer reason;
        private String remark;

        // 数据权限范围，服务端根据当前登录角色推导后覆盖写入，前端传值无效（管理员为 null 表示不限）
        private Integer scopeUserId;

}

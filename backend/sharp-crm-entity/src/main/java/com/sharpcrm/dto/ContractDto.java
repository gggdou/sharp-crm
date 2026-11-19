package com.sharpcrm.dto;

import lombok.Data;

/**
 * 合同查询与签订参数
 */
@Data
public class ContractDto {

    private String contractNo;
    private Integer customerId;
    private Integer status;

    private Integer page = 1;
    private Integer pageSize = 10;

    /** 数据权限范围，服务端推导后覆盖写入，前端传值无效 */
    private Integer scopeUserId;

    // ===== 签订时使用，不参与查询 =====
    /** 分期数，payType=2 时生效，缺省 3 期 */
    private Integer periods;
}

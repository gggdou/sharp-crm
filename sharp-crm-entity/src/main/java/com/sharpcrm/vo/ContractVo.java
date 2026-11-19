package com.sharpcrm.vo;

import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同详情：合同主体 + 每期回款 + 汇总指标
 */
@Data
public class ContractVo {

    private Contract contract;

    private String customerName;

    /** 每期回款计划与实收，按期次升序 */
    private List<Payment> payments;

    /** 已回款金额 */
    private BigDecimal paidAmount;

    /** 未回款金额 */
    private BigDecimal unpaidAmount;

    /** 回款进度百分比 0-100 */
    private Integer progress;

    /** 逾期期数 */
    private Integer overdueCount;
}

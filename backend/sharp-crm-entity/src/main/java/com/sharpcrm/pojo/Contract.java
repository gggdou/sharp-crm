package com.sharpcrm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同：客户成交后的正式签约记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("contract")
public class Contract {

    private Integer id;
    private String contractNo;      // 合同编号，业务唯一
    private Integer customerId;     // 客户id
    private Integer businessId;     // 来源商机id
    private Integer courseId;       // 成交课程id
    private BigDecimal amount;      // 合同金额
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate signDate;     // 签订日期
    private Integer status;         // 1草稿 2生效 3已完成 4已作废
    private Integer payType;        // 1一次性 2分期
    private Integer userId;         // 签约人
    private String remark;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ===== 列表展示用的派生字段，不落库 =====
    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String ownerName;
    @TableField(exist = false)
    private BigDecimal paidAmount;   // 已回款金额
    @TableField(exist = false)
    private Integer periodCount;     // 回款期数
}

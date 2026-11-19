package com.sharpcrm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同回款计划：一期一条，既记录计划金额日期，也记录实收
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment")
public class Payment {

    private Integer id;
    private Integer contractId;     // 合同id
    private Integer period;         // 第几期，从 1 开始
    private BigDecimal planAmount;  // 计划回款金额
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate planDate;     // 计划回款日期
    private BigDecimal actualAmount;// 实收金额，未收为 null
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate actualDate;   // 实收日期
    private Integer status;         // 1待回款 2部分回款 3已回款 4逾期
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

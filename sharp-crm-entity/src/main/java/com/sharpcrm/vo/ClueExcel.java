package com.sharpcrm.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 线索 Excel 导入导出模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClueExcel {

    @ExcelProperty("客户姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("渠道来源(1线上活动/2推广介绍)")
    private Integer channel;

    @ExcelProperty("意向学科(1-7)")
    private Integer subject;

    @ExcelProperty("意向等级(1-4)")
    private Integer level;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty("性别(1男/2女)")
    private Integer gender;

    @ExcelProperty("微信号")
    private String wechat;

    @ExcelProperty("备注")
    private String remark;
}

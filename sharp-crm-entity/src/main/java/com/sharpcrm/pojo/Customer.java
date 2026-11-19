package com.sharpcrm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户实体：商机转化后的正式客户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer")
public class Customer {
    private Integer id;
    private String name;          // 客户姓名
    private String phone;         // 手机号
    private Integer gender;       // 性别 1男 2女
    private Integer age;          // 年龄
    private String wechat;        // 微信号
    private String qq;            // QQ 号
    private Integer degree;       // 学历 1高中 2中专 3大专 4本科 5硕士 6博士 7其他
    private Integer jobStatus;    // 在职情况 1在职 0离职
    private Integer channel;      // 渠道来源 1线上活动 2推广介绍
    private Integer subject;      // 意向学科
    private Integer courseId;     // 成交课程
    private Integer userId;       // 归属人
    private Integer businessId;   // 来源商机
    private Integer clueId;       // 来源线索
    private String remark;        // 备注
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

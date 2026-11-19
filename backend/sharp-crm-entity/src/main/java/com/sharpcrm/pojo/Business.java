package com.sharpcrm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("business")
public class Business {
    private Integer id; // 商机id, 主键
    @jakarta.validation.constraints.NotBlank(groups = com.sharpcrm.validation.Create.class, message = "客户姓名不能为空")
    @jakarta.validation.constraints.Size(max = 20, message = "客户姓名不能超过 20 个字符")
    private String name; // 客户姓名
    @jakarta.validation.constraints.NotBlank(groups = com.sharpcrm.validation.Create.class, message = "手机号不能为空")
    @jakarta.validation.constraints.Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone; // 手机号
    private Integer gender; // 性别，1:男, 2:女
    private Integer age; // 年龄
    private String wechat; // 微信号
    private String qq; // qq号
    private Integer subject; // 意向学科，1:AI智能应用开发(java), 2:AI大模型开发(python)，3:AI鸿蒙开发，4:AI大数据，5:AI嵌入式，6:AI测试，7:AI运维
    private Integer courseId; // 意向课程, 课程id
    private Integer degree; // 学历, 1:高中、2:中专、3:大专、4:本科、5:硕士、6:博士、7:其他
    private Integer jobStatus; // 在职情况, 1: 在职, 0: 离职
    private Integer channel; // 渠道来源，1:线上活动, 2:推广介绍
    private String remark; // 备注
    private Integer status; // 商机状态，1:待分配, 2:待跟进, 3:跟进中, 4:回收, 5:转客户
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer userId; // 归属人id，关联用户id
    private Integer clueId; // 归属线索id
    private LocalDateTime nextTime; // 下次跟进时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 修改时间

    /**
     * 乐观锁版本号，防止多人同时跟进同一商机时互相覆盖
     */
    @com.baomidou.mybatisplus.annotation.Version
    private Integer version;

    @TableField(exist = false)
    private String assignName; // 分配人姓名
    @TableField(exist = false)
    private List<BusinessTrackRecord> trackRecords; // 商机跟进记录

    // ========== 新增前端跟进接口的临时参数（数据库business表不存在） ==========
    @TableField(exist = false)
    private Integer trackStatus; // 跟进状态
    @TableField(exist = false)
    private List<String> keyItems; // 跟进要点，数组
    @TableField(exist = false)
    private String record; // 跟进内容
}

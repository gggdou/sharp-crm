package com.sharpcrm.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("user")
public class User {
    private Integer id;
    @jakarta.validation.constraints.NotBlank(groups = com.sharpcrm.validation.Create.class, message = "用户名不能为空")
    @jakarta.validation.constraints.Size(min = 3, max = 20, message = "用户名长度需在 3 到 20 个字符之间")
    private String username;
    @jakarta.validation.constraints.NotBlank(groups = com.sharpcrm.validation.Create.class, message = "密码不能为空")
    @jakarta.validation.constraints.Size(min = 6, max = 32, message = "密码长度需在 6 到 32 个字符之间")
    private String password;
    private String name;
    @jakarta.validation.constraints.Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    private String email;
    private Integer gender;
    private Integer status;
    private Integer deptId;
    private Integer roleId;
    private String image;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
//    private Integer start; 用mp Ipage进行多表查询 不用该属性
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String roleName;
    @TableField(exist = false)
    private String roleLabel;

}

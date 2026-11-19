use sharp_crm
-- 角色表
CREATE TABLE `role`
(
    `id`          int unsigned primary key AUTO_INCREMENT COMMENT '角色id, 主键',
    `name`        varchar(10)  NOT NULL unique COMMENT '角色名称',
    `label`       varchar(20)  NOT NULL unique COMMENT '角色标识',
    `remark`      varchar(50) DEFAULT NULL COMMENT '备注',
    `create_time` datetime     NOT NULL COMMENT '创建时间',
    `update_time` datetime     NOT NULL COMMENT '修改时间'
) COMMENT ='角色表';
INSERT INTO `role` (`id`, `name`, `label`, `remark`, `create_time`, `update_time`)
VALUES (1, '管理员', 'admin', '管理员, 用于管理整个系统数据', '2025-05-09 19:47:28', '2025-05-09 19:47:28'),
       (2, '线索专员', 'clue_operator', '线索专员', '2025-05-09 19:48:00', '2025-05-09 19:48:00'),
       (3, '商机专员', 'business_operator', '商机专员', '2025-05-09 20:03:04', '2025-05-09 20:03:04'),
       (4, '普通用户', 'common_user', '普通公司员工', '2025-06-21 15:25:30', '2025-08-06 21:36:23'),
       (9, '测试角色', 'role_test', '测试角色', '2025-08-06 21:35:30', '2025-08-06 21:35:41');
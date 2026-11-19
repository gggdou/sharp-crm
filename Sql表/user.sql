use sharp_crm;

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
VALUES
    (1, '管理员', 'admin', '管理员, 用于管理整个系统数据', '2025-05-09 19:47:28', '2025-05-09 19:47:28'),
    (2, '线索专员', 'clue_operator', '线索专员', '2025-05-09 19:48:00', '2025-05-09 19:48:00'),
    (3, '商机专员', 'business_operator', '商机专员', '2025-05-09 20:03:04', '2025-05-09 20:03:04'),
    (4, '普通用户', 'common_user', '普通公司员工', '2025-06-21 15:25:30', '2025-08-06 21:36:23'),
    (5, '技术开发', 'dev_engineer', '负责项目代码开发与维护', '2025-07-02 10:20:00', '2025-07-02 10:20:00'),
    (6, '测试工程师', 'qa_tester', '功能测试、bug验证，保障系统稳定', '2025-07-02 10:25:00', '2025-07-02 10:25:00'),
    (7, '运维管理员', 'ops_admin', '服务器部署、环境维护与监控', '2025-07-02 10:30:00', '2025-07-02 10:30:00'),
    (8, '产品经理', 'product_manager', '需求调研，产品功能规划', '2025-07-02 10:35:00', '2025-07-02 10:35:00'),
    (9, '测试角色', 'role_test', '测试角色', '2025-08-06 21:35:30', '2025-08-06 21:35:41');

-- 用户表
create table user
(
    id          int unsigned primary key auto_increment comment 'id, 主键',
    username    varchar(20)      not null unique comment '用户名，唯一',
    password    varchar(64)      not null comment '密码',
    name        varchar(20)      not null comment '姓名',
    phone       char(11)         not null unique comment '手机号，唯一',
    email       varchar(50)      not null unique comment '邮箱，唯一',
    gender      tinyint unsigned not null comment '性别，1: 男，2: 女',
    status      tinyint unsigned not null comment '状态，1: 正常，0: 停用',
    dept_id     int unsigned comment '部门id，关联部门表主键',
    role_id     int unsigned comment '角色id，关联角色表主键',
    image       varchar(255) comment '头像url',
    remark      varchar(50) comment '备注，50字以内',
    create_time datetime         not null comment '创建时间',
    update_time datetime         not null comment '修改时间'
) comment '用户表';

INSERT INTO user (id, username, password, name, phone, email, gender, status, dept_id, role_id, image, remark,
                  create_time, update_time)
VALUES (1, 'zhangsan', '4e7bdb88640b376ac6646b8f1ecfb558', '张三', '13800138001', 'admin@example.com', 1, 1, 6, 1,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/ee089310-479b-4672-a74a-dc680fa6a18a.png', '系统管理员',
        '2025-05-12 11:39:03', '2025-06-21 15:25:55'),
       (2, 'lisi', 'c3cb6d12c40908943b64bc0681af47db', '李四', '13800138002', 'editor1@example.com', 1, 1, 4, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/62fcb582-074c-489a-8ea8-721bc8bba042.png', '内容编辑',
        '2025-05-12 11:39:03', '2025-06-21 15:26:00'),
       (3, 'sunwuji', '368a7fccd730d807f41e2161798e13ca', '孙无忌', '18809091111', 'sunwuji@163.com', 1, 1, 5, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/e65781f3-c87d-46e2-9d53-cf4329ce64e6.png',
        '孙无忌刚入职，先在销售部磨炼磨炼', '2025-05-12 16:46:21', '2025-07-14 18:13:45'),
       (4, 'songjiang', 'a91b1cfe31e6b534537ab992e06380f8', '宋江', '13800000001', 'songjiang@example.com', 1, 1, 5, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/0bdd90db-17e1-407c-b922-27d987c9cc45.png', 'Remark 1',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (5, 'lujunyi', 'ef73dd11149e096116f755a171eb3fec', '卢俊义', '13800000002', 'lujunyi@example.com', 1, 1, 3, 7,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/1564f4d9-7d07-4ea4-9e23-ecebe39d3520.png', 'Remark 2',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (6, 'wuyong', 'd74bcf7b7f805922e4ad42315f3a8cde', '吴用', '13800000003', 'wuyong@example.com', 1, 0, 7, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/cb074a33-4f30-4b3e-b9f1-bf8bf9d0f4ff.png', 'Remark 3',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (7, 'gongsunsheng', 'ee3a229f2f3d2bd7c9dd3eeb4eda02b3', '公孙胜', '13800000004', 'gongsunsheng@example.com', 1,
        1, 5, 2, 'https://web2025-test.oss-cn-beijing.aliyuncs.com/284efce8-0b0c-4076-bbf6-3e104446ce70.png',
        'Remark 4', '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (8, 'linchong', '786271307576f1ee762341173a2caa48', '林冲', '13800000006', 'linchong@example.com', 1, 1, 4, 3,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/d2fbc991-c6de-4871-bd91-4a7d186b1f90.png', 'Remark 6',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (9, 'qinming', '23b9c4ea1f4d1844acd20a5314da8553', '秦明', '13800000007', 'qinming@example.com', 1, 1, 5, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/d87de2cc-79a4-4a56-af9c-eb6aca52db66.png', 'Remark 7',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (10, 'huarong', '19d59a4e1e63552228b73b2cb2b5fcdd', '花荣', '13800000008', 'huayong@example.com', 1, 1, 6, 1,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/4775c1a5-4944-4aec-9753-9fa622dbbe75.png', 'Remark 8',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (11, 'huyan', 'fbb39f3019cc4f5444dbd6b623f71a1a', '呼延灼', '13800000009', 'huyan@example.com', 1, 0, 8, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/446aa6db-85b0-451f-bc11-92a52a518192.png', 'Remark 9',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (12, 'huaqian', '1007f13423a57bc0390d9510f96d0322', '花千朵', '13800000010', 'huaqian@example.com', 2, 1, 10, 3,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/63137d09-0734-4910-9502-b9b8d063b132.png', 'Remark 10',
        '2025-05-14 15:49:31', '2025-07-14 18:13:45'),
       (13, 'shaqianmo', '29d6fb85ac49d6d82b9b6dfd5f62939a', '杀阡陌', '13509091456', 'shaqianmo@163.com', 0, 1, 5, 2,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/ff8646a3-629b-4124-a2cb-f0365f9075c2.png', '',
        '2025-06-19 17:48:39', '2025-07-14 18:13:45'),
       (14, 'baizihua', 'f1659bd8a56022d817d6bd124c6181e8', '白子画', '13609091206', 'baizihua@163.com', 1, 1, 3, 7,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/421c0945-6f25-405c-b439-9b8b997338aa.png', '系统研发',
        '2025-06-22 14:33:49', '2025-07-14 18:13:45'),
       (15, 'huaqiangu', 'be6485aab12634baf922b3dbe407fcda', '花千骨', '15809092819', 'huaqiangu@163.com', 0, 1, 6, 7,
        'https://web2025-test.oss-cn-beijing.aliyuncs.com/735320dc-ac08-4b73-bdd7-7f0cf054274f.png',
        '花千骨是一个普通用户', '2025-06-24 10:40:20', '2025-07-14 18:13:45'),
       (16, 'kuangyetian', '5e3c47c0d266439d7ce704972c92086a', '旷野天', '13689281921', 'kuangyetian@163.com', 1, 1, 9,
        7, 'https://web2025-test.oss-cn-beijing.aliyuncs.com/5d1f8e0f-2227-4469-8c90-7a02867fbe70.png',
        '新入职的技术员旷野天', '2025-06-27 19:38:34', '2025-07-14 18:13:45');
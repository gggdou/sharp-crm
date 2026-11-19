create database sharp_crm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
use sharp_crm;

-- 部门表
create table dept(
    id          int unsigned primary key auto_increment comment '部门id，主键',
    name        varchar(10)      not null unique comment '部门名称',
    status      tinyint unsigned not null default 1 comment '状态：0-停用，1-正常',
    create_time datetime         not null comment '创建时间',
    update_time datetime         not null comment '修改时间'
) comment '部门信息表';

-- 测试数据
insert into dept(name, status, create_time, update_time) values
('学工部', 1, '2023-07-01 10:00:00', '2023-07-01 10:00:00'),
('教研部', 1, '2023-07-01 10:00:00', '2023-07-01 10:00:00'),
('人事部', 1, '2023-07-01 10:00:00', '2023-07-01 10:00:00');
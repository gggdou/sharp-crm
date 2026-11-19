use sharp_crm;

-- 客户表（商机 toCustomer 后生成）
CREATE TABLE IF NOT EXISTS customer
(
    id          int unsigned primary key auto_increment comment '客户id, 主键',
    phone       varchar(11)      not null comment '手机号',
    name        varchar(20)      not null comment '客户姓名',
    channel     tinyint unsigned comment '渠道来源，1:线上活动, 2:推广介绍',
    gender      tinyint unsigned comment '性别，1:男, 2:女',
    age         tinyint unsigned comment '年龄',
    wechat      varchar(50) comment '微信号',
    qq          varchar(20) comment 'qq号',
    degree      tinyint unsigned comment '学历, 1:高中、2:中专、3:大专、4:本科、5:硕士、6:博士、7:其他',
    job_status  tinyint unsigned comment '在职情况, 1: 在职, 0: 离职',
    subject     tinyint unsigned comment '意向学科，1:ai智能应用开发(java), 2:ai大模型开发(python)，3:ai鸿蒙开发，4:ai大数据，5:ai嵌入式，6:ai测试，7:ai运维',
    course_id   int unsigned comment '成交课程id',
    user_id     int unsigned comment '归属人id',
    business_id int unsigned comment '来源商机id',
    clue_id     int unsigned comment '来源线索id',
    remark      varchar(200) comment '备注',
    create_time datetime comment '创建时间',
    update_time datetime comment '修改时间',
    unique key uk_phone (phone)
) comment '客户表';

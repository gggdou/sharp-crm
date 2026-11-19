-- ============================================================
-- 合同与回款
-- 合同挂在客户下，回款按合同分期。CRM 里「有客户没合同」等于没有成交闭环，
-- 这两张表补的就是「钱」这一段。
-- ============================================================

CREATE TABLE IF NOT EXISTS `contract` (
  `id`          int NOT NULL AUTO_INCREMENT COMMENT '合同id',
  `contract_no` varchar(32)  NOT NULL COMMENT '合同编号，业务唯一',
  `customer_id` int          NOT NULL COMMENT '客户id',
  `business_id` int          DEFAULT NULL COMMENT '来源商机id',
  `course_id`   int          DEFAULT NULL COMMENT '成交课程id',
  `amount`      decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '合同金额',
  `sign_date`   date         DEFAULT NULL COMMENT '签订日期',
  `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '1草稿 2生效 3已完成 4已作废',
  `pay_type`    tinyint      NOT NULL DEFAULT '1' COMMENT '1一次性 2分期',
  `user_id`     int          DEFAULT NULL COMMENT '签约人/归属人',
  `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
  `version`     int          NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `create_time` datetime     DEFAULT NULL,
  `update_time` datetime     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_contract_customer` (`customer_id`),
  KEY `idx_contract_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同';

CREATE TABLE IF NOT EXISTS `payment` (
  `id`            int NOT NULL AUTO_INCREMENT COMMENT '回款id',
  `contract_id`   int          NOT NULL COMMENT '合同id',
  `period`        int          NOT NULL COMMENT '第几期，从1开始',
  `plan_amount`   decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '计划回款金额',
  `plan_date`     date         DEFAULT NULL COMMENT '计划回款日期',
  `actual_amount` decimal(12,2) DEFAULT NULL COMMENT '实收金额',
  `actual_date`   date         DEFAULT NULL COMMENT '实收日期',
  `status`        tinyint      NOT NULL DEFAULT '1' COMMENT '1待回款 2部分回款 3已回款 4逾期',
  `remark`        varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time`   datetime     DEFAULT NULL,
  `update_time`   datetime     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_payment_contract` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同回款计划与实收';

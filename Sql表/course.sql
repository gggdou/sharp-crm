
use qk;
-- 课程表
create table course (
                        id int unsigned primary key auto_increment comment '课程id, 主键',
                        subject tinyint unsigned not null comment '课程学科，1:AI智能应用开发(Java), 2:AI大模型开发(Python)，3:AI鸿蒙开发，4:AI大数据，5:AI嵌入式，6:AI测试，7:AI运维',
                        name varchar(20) not null comment '课程名称',
                        price int unsigned not null comment '课程价格（元）',
                        target tinyint unsigned not null comment '适用人群, 1:小白学员, 2:初级程序员, 3:中级程序员',
                        description varchar(100) comment '课程介绍',
                        create_time datetime not null comment '创建时间',
                        update_time datetime not null comment '修改时间'
) comment '课程表';

INSERT INTO course (id, subject, name, price, target, description, create_time, update_time) VALUES
                                                                                                 (1,1,'Java核心与AI开发基础',28,1,'Java核心与AI开发基础','2025-05-09 21:29:35','2025-05-24 10:31:53'),
                                                                                                 (2,2,'Python核心与AI开发基础',39,1,'主要讲解Python核心与AI开发基础','2025-05-09 21:30:00','2025-05-09 21:42:32'),
                                                                                                 (3,1,'AI驱动Web开发',2800,2,'AI驱动Web开发， 主要讲解Web开发的核心知识及Web项目的设计、开发、测试、部署','2025-05-22 11:01:40','2025-05-22 11:01:55'),
                                                                                                 (4,1,'企业级物联网项目',4500,2,'企业级物联网项目，主要讲解物联网项目的设计、开发、测试、部署全流程交付','2025-05-22 11:02:44','2025-05-22 11:02:44'),
                                                                                                 (6,1,'SpringAI大模型应用开发',1990,3,'SpringAI大模型应用开发','2025-05-22 11:08:26','2025-05-22 11:08:26'),
                                                                                                 (7,2,'数据分析',2000,2,'基于Python数据分析的','2025-05-22 21:44:49','2025-05-22 21:45:50'),
                                                                                                 (8,3,'前端基础',28,1,'前端基础，主要讲解HTML、CSS、JS等前端开发基础知识','2025-05-23 20:07:47','2025-05-23 20:07:47'),
                                                                                                 (9,2,'LangChain入门',1800,2,'LangChain入门，学习该框架如何操作AI大模型','2025-05-26 21:43:57','2025-05-26 21:44:26'),
                                                                                                 (10,1,'LangChain4j',299,2,'LangChain4j从入门到进阶, 适合AI初学者','2025-06-19 19:03:17','2025-06-19 19:03:17'),
                                                                                                 (11,1,'RAG增强检索',399,2,'RAG','2025-06-19 19:04:24','2025-06-19 19:04:24'),
                                                                                                 (12,1,'SpringCloud微服务框架',1200,2,'SpringCloud微服务框架及分布式解决方案','2025-06-22 14:41:16','2025-06-22 14:41:16'),
                                                                                                 (13,1,'微服务智能项目集',3899,2,'微服务智能项目集','2025-06-22 14:44:06','2025-06-22 14:44:06'),
                                                                                                 (14,1,'AI智能体项目-天机AI助理',4999,2,'AI智能体项目-天机AI助理','2025-06-22 14:44:31','2025-06-22 14:44:31');
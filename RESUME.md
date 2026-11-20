# 锐客 CRM（sharp-crm） —— 简历项目材料

> 本文档用于简历撰写与面试准备，内容基于项目真实代码，可直接摘取使用。

## 一、简历项目描述（可直接粘贴）

**项目名称**：锐客 CRM 招生管理系统
**项目角色**：后端负责人 / 全栈开发
**技术栈**：Spring Boot 3.4.8、MyBatis-Plus 3.5.17、MySQL 8、Redis、JDK 17、JWT、EasyExcel、Spring Task、ThreadPoolExecutor、Knife4j、阿里云 OSS、Vue 3、Element Plus、Pinia、Vite、Docker、Nginx

**项目描述**：
面向 AI 职业教育培训机构的客户关系管理系统，覆盖「市场活动 → 线索 → 商机 → 客户」完整招生转化链路，含用户/部门/角色权限体系、课程管理、操作审计与数据统计。采用多模块聚合架构（common/entity/management），前后端分离部署。

**核心职责与亮点**：
1. 负责线索、商机、客户核心业务模块设计开发，实现线索分配、跟进留痕、伪线索标记、商机转客户等完整状态流转，通过事务保证数据一致性。
2. 基于 Redis 实现课程列表缓存、验证码与缓存穿透防护，设计本地降级方案保证缓存服务异常时业务可用。
3. 引入 ThreadPoolExecutor + @Async 将操作日志异步落库，避免审计写入阻塞主业务响应；使用 CallerRunsPolicy 保证日志不丢失。
4. 基于 Spring Task 实现公海线索定时自动回收，解决线索长期无人跟进的呆滞问题。
5. 使用 EasyExcel 实现线索批量导入导出，支持大文件流式读取与事务化批量入库。
6. 基于 JWT + 拦截器 + ThreadLocal 实现无状态登录鉴权与用户上下文传递；Knife4j 规范化接口文档。
7. 编写 Dockerfile + docker-compose 实现 MySQL、Redis、后端、Nginx 一键编排部署。
8. 主导 Vue3 前端重建，基于 Element Plus 组件化封装通用 CRUD 表格，ECharts 实现招生转化看板。

## 二、技术亮点详解（面试讲透这些）

### 亮点1：Redis 缓存与本地降级
- **场景**：课程列表高频读取，直接查库浪费资源。
- **实现**：`RedisCache` 统一封装，读时先查 Redis，未命中查库回填；`findAll` 缓存 30 分钟，增删改时删除缓存保证一致性。
- **穿透防护**：查询为空时写入 `__NULL__` 标记 + 短过期时间，防止缓存穿透。
- **降级设计**：`RedisCache.redisAvailable()` 探活，Redis 不可用时自动切本地内存缓存——这是**可用性优先**的取舍，保证单测和开发环境无需 Redis 也能跑通。
- **可讲深度**：缓存三兄弟（穿透/击穿/雪崩）、双写一致性策略、为什么选择删除缓存而非更新缓存。

### 亮点2：异步操作日志
- **场景**：每个增删改都要写审计日志，同步写库增加接口耗时。
- **实现**：`LogAspect` 环绕通知拦截 `add*/update*/delete*` 方法，组装日志对象后调用独立 Bean `AsyncLogServiceImpl.saveAsync()`（`@Async("asyncExecutor")`）。
- **关键坑点1**：`@Async` 同类内部调用不生效（Spring AOP 自调用问题），必须拆成独立 Bean——这是面试常考点。
- **关键坑点2**：异步线程拿不到 ThreadLocal，所以操作人 ID 必须在主线程（切面里）先取出再传入。
- **线程池参数**：核心线程数 = CPU 核数，队列 500，`CallerRunsPolicy` 拒绝策略保证日志不丢，daemon 线程 + 空闲回收避免资源泄漏。

### 亮点3：公海定时回收
- **场景**：分配给销售但超过 7 天未跟进的线索会呆滞浪费。
- **实现**：`@Scheduled(cron = "0 0 2 * * ?")` 每天凌晨 2 点扫描 `status=2 且 update_time < now-7d` 的线索，批量置空 userId、status 改回 1（公海）。
- **边界说明**：项目为单体部署，定时任务仅单实例运行。`RedisCache` 封装了 `setIfAbsent` 原子原语，但任务本身未加锁；若未来多实例部署，可用它实现互斥锁避免重复执行。面试如实说明即可，不要声称已实现分布式锁。

### 亮点4：EasyExcel 批量导入导出
- **场景**：销售需要从 Excel 批量导入线索、导出数据做分析。
- **实现**：独立 `ClueExcel` 模型 + `@ExcelProperty` 注解，导入用 `ReadListener` 逐行收集后批量入库（事务），导出直接 `EasyExcel.write().doWrite()`。
- **可讲深度**：为什么用 EasyExcel 而非 POI（内存占用低，Sax 模式流式读取，避免 big file OOM）。

### 亮点5：JWT 无状态鉴权
- **实现**：登录校验密码 MD5 + 状态，签发 JWT（载荷 id/username/name/image）；`LoginInterceptor` 校验请求头 token，ThreadLocal 传递用户上下文，`afterCompletion` 清理防止内存泄漏。
- **可讲深度**：JWT  vs Session、token 过期与续签、为什么放 Header 不放 Cookie（跨域/CSRF）。

### 亮点6：多模块聚合架构
- **设计**：parent（pom 聚合 + dependencyManagement 统一版本）、common（工具类/缓存/配置）、entity（POJO/DTO/VO）、management（业务/Web 入口）。
- **可讲深度**：依赖如何分层避免循环、dependencyManagement 与 dependencies 区别、为什么这样拆模块。

## 三、高频面试问答

**Q1：项目难点是什么？怎么解决的？**
> 线索状态流转复杂（待分配/跟进中/伪线索/转商机/转客户），多个表联动。用事务 + 状态枚举统一管理，转商机、转客户时用 `@Transactional` 保证主表状态与从表记录同时成功或回滚。另外操作日志写库拖慢接口，用线程池异步化解决。

**Q2：Redis 缓存和数据库不一致怎么办？**
> 采用「先更新数据库，再删除缓存」策略。删除而非更新缓存，是因为更新缓存可能产生脏数据（并发写覆盖）。极端情况下的短暂不一致可接受，业务上课程信息变更不频繁。还设置了过期时间作为兜底。

**Q3：@Async 为什么不生效？**
> 三种常见原因：① 同类内部方法调用（走的是目标对象方法，没走代理）；② 没加 @EnableAsync；③ 异步方法必须是 public 且返回值是 void 或 Future。本项目踩过第①个坑，拆成独立 Bean 解决。

**Q4：线程池参数怎么设的？为什么？**
> CPU 密集型核心线程数 = CPU 核数，IO 密集型 = 2×CPU 核数。本项目日志写库是 IO 密集，核心线程数设为 CPU 核数、最大 2 倍。队列用有界队列 500 防止无限堆积 OOM，拒绝策略用 CallerRunsPolicy 让调用方执行，保证日志不丢。

**Q5：公海回收定时任务在多实例部署下有什么问题？**
> 项目当前是单体单实例，不涉及重复执行。若未来扩展为多实例，确实会重复执行——解法是加 Redis 分布式锁（`setIfAbsent` 带过期时间）保证只有一个实例执行，或改用 XXL-Job 等分布式调度框架。项目封装了 `setIfAbsent` 原语但任务未实际加锁，面试如实说明这一点，不要说成已实现，避免被追问穿帮。

**Q6：为什么用 EasyExcel 不用 Apache POI？**
> POI 用 DOM 方式解析，大文件会把整个 Excel 读进内存，容易 OOM。EasyExcel 基于 SAX 流式读取，逐行解析，内存占用低，百万级数据也能处理。

**Q7：JWT 注销后 token 还能用怎么办？**
> JWT 无状态，服务端不保存。解决方案：① 前端删除 token（治标）；② 服务端用 Redis 维护 token 黑名单，设过期时间；③ 缩短 token 有效期 + refresh token 机制。本项目 Redis 工具已具备黑名单能力。

**Q8：项目如何部署？**
> Dockerfile 多阶段构建（Maven 打包 + JRE 运行减小镜像体积），docker-compose 编排 MySQL、Redis、backend、Nginx 四个服务，Nginx 做静态资源 + /api 反向代理，一条命令 `docker-compose up -d` 启动全栈。

## 四、简历话术模板（STAR 法则示例）

**情境(S)**：原系统课程信息每次查询都直连数据库，高并发下数据库压力大；每个增删改操作同步写审计日志，接口响应慢。

**任务(T)**：需在保证数据一致性与审计完整性的前提下，优化查询性能与接口响应速度。

**行动(A)**：
- 引入 Redis 缓存课程列表，读多写少场景缓存 30 分钟，更新时删除缓存；设计空值标记防穿透、本地内存降级保可用。
- 用 ThreadPoolExecutor + @Async 异步化操作日志，拆独立 Bean 解决自调用失效，ThreadLocal 数据主线程传递。

**结果(R)**：课程查询数据库访问量显著下降，写操作接口响应更快，审计日志零丢失，开发环境无需 Redis 即可运行测试。

## 五、项目数据（简历可量化）

- 后端：4 个 Maven 模块，约 80+ Java 源文件，10+ REST Controller
- 前端：Vue3 + Element Plus，10 个页面，通用 CRUD 组件化封装
- 测试：19 个单元测试，覆盖工具类、登录注册、线索转商机
- 部署：Docker 编排 4 个服务，Nginx 统一入口

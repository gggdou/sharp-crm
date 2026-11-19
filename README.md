# 锐客 CRM（sharp-crm）

面向 **AI 职业教育培训机构** 的招生客户关系管理系统，覆盖「市场活动 → 线索 → 商机 → 客户」完整转化链路，前后端分离，含 Redis 缓存、异步审计、Excel 批量导入导出与容器化部署。

## 一、项目简介

培训机构的招生团队每天要处理大量来自各渠道的潜在学员信息。人工作业的问题集中在三点：线索散落无法统一分配、跟进过程无留痕导致撞单丢单、转化结果无法沉淀为可复用的客户资产。本系统围绕这条主线设计：

- **线索集中管理**：汇总各渠道（市场活动、推广介绍）来的潜在学员信息，统一入池、分配、跟进；
- **转化过程管控**：线索可标记伪线索、转为商机；商机可分配、跟进、踢回公海；商机最终可转客户沉淀；
- **过程可追溯**：跟进记录表留存每次沟通，AOP 切面异步记录所有写操作到 `operate_log`；
- **后台支撑**：用户、部门、角色构成 RBAC 基础，课程管理维护在售课程，OSS 承载图片资源。

**转化链路**

```
市场活动 activity ──> 线索 clue ──(toBusiness)──> 商机 business ──(toCustomer)──> 客户 customer
                        │                            │
                        └──(false)──> 伪线索          └──(back)──> 公海池
```

**状态流转**

- 线索：`1 待分配 → 2 跟进中 → 3 伪线索 / 4 转商机`
- 商机：`1 待分配 → 2 待跟进 → 3 跟进中 → 4 回收公海 / 5 转客户`

## 二、技术栈

| 分层 | 技术选型 |
|---|---|
| 语言 / 运行时 | Java 17 |
| 后端框架 | Spring Boot 3.4.8（MVC + AOP）、Spring Task 定时任务 |
| 持久层 | MyBatis-Plus 3.5.17（分页插件 jsqlparser）、MySQL 8、XML 复杂查询 |
| 缓存 | Redis（Lettuce）+ 自定义 `RedisCache`（含本地降级） |
| 鉴权 | JWT（jjwt 0.9.1，HS256）+ 拦截器 + ThreadLocal 上下文 |
| 并发 | `ThreadPoolExecutor` + `@Async` 异步落库 |
| 文档 | Knife4j 4.5.0（OpenAPI 3） |
| 导入导出 | EasyExcel 4.0.3（流式读写） |
| 工具库 | Hutool 5.8.44、Lombok |
| 文件存储 | 阿里云 OSS SDK 3.18.4（凭证走环境变量） |
| 日志 | Slf4j + Logback（traceId 贯穿 + 滚动文件） |
| 可观测性 | Spring Boot Actuator（健康检查 / 指标）、自定义慢 SQL 拦截器 |
| 前端 | Vue 3.5 + Vite 6 + Element Plus 2.9 + Pinia + Vue Router 4 + Axios + ECharts 5 |
| 部署 | Nginx 1.24 反向代理、Docker 多阶段构建、docker-compose 编排 |
| 构建 | Maven 多模块聚合（sharp-crm-parent / common / entity / management）、GitHub Actions |
| 测试 | JUnit 5 + Mockito（30 个单元测试，不依赖外部服务） |

## 三、工程结构

```
sharp-crm/
├── backend/                        后端（Maven 多模块聚合）
│   ├── pom.xml                     父工程，统一依赖与版本管理
│   ├── sharp-crm-common/           公共能力（被两个模块复用）
│   │   └── com.sharpcrm.common
│   │       ├── Result / PageDate     统一返回结构与分页对象
│   │       ├── JWTUtils              JWT 签发与解析
│   │       ├── DataHolder            ThreadLocal 用户上下文
│   │       ├── RedisCache            Redis 读写封装（异常自动降级本地缓存）
│   │       ├── VerifyCodeUtils       图形验证码生成
│   │       ├── OSSUtils              阿里云 OSS 上传
│   │       ├── AliProperties         OSS 配置绑定
│   │       └── config/               Redis 序列化配置、线程池配置
│   ├── sharp-crm-entity/           领域模型
│   │   └── com.sharpcrm.{pojo,dto,vo}
│   │       ├── pojo/                 13 个实体（User/Dept/Role/Clue/Business/Customer/Contract/Payment/...）
│   │       ├── dto/                  查询条件对象
│   │       └── vo/                   LoginVo、ClueExcel、DashboardVo、Customer360Vo、TodoVo、ContractVo
│   └── sharp-crm-management/       Web 主模块（启动入口）
│       └── com.sharpcrm
│           ├── App.java              启动类（开启 AOP、定时任务）
│           ├── controller/           14 个 Controller
│           ├── service/ + impl/      业务接口与实现
│           ├── mapper/               MyBatis-Plus Mapper
│           ├── interceptor/          LoginInterceptor 登录拦截
│           ├── aspect/               LogAspect 操作审计、RoleAuthAspect 角色鉴权
│           ├── support/              DataScopeHelper 数据权限推导
│           ├── task/                 ClueRecycleTask 线索回收、ContractOverdueTask 回款逾期
│           ├── config/               Web / MyBatis-Plus / Knife4j / 字段填充 / traceId / 慢 SQL
│           ├── exception/            业务异常与全局异常处理
│           └── resources/            application.yml、logback.xml、5 个 Mapper XML
├── frontend/                       前端（Vue 3 + Vite）
│   └── src
│       ├── api/index.js              全部接口定义（含 CRUD 工厂）
│       ├── utils/request.js          Axios 封装（token 注入、统一错误处理）
│       ├── store/user.js             Pinia 用户状态
│       ├── router/index.js           路由与登录守卫
│       ├── layout/index.vue          侧边栏 + 顶栏布局
│       ├── components/SimpleCrud.vue 配置驱动的通用 CRUD 表格组件
│       └── views/                    11 个页面（登录、仪表盘、线索、商机、客户...）
└── ops/                             部署与运维物料
    ├── Dockerfile                    后端多阶段构建（构建上下文为仓库根）
    ├── docker-compose.yml            mysql / redis / backend / nginx 四服务编排
    ├── docker/nginx.conf             容器内 Nginx 反向代理配置
    ├── nginx/                        本地 Nginx 1.24 发行版（生产形态演示）
    ├── sql/                          建库与初始化脚本（按序号执行）
    ├── scripts/bench.py              接口压测脚本
    └── docs/压测报告.md              压测环境、数据与结论
```


## 四、快速开始

### 4.1 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 17 |
| Maven | 3.6+ |
| MySQL | 8.x |
| Node.js | 18+（前端构建） |
| Redis | 可选，未启动时自动降级为本地缓存 |

### 4.2 初始化数据库

按顺序执行 `ops/sql/` 下脚本（均以 `use sharp_crm` 开头）：

| 顺序 | 脚本 | 内容 |
|---|---|---|
| 1 | `data.sql` | 建库 `sharp_crm`、部门表及数据 |
| 2 | `role.sql` | 角色表及数据 |
| 3 | `user.sql` | 用户表及数据 |
| 4 | `course.sql` | 课程表及数据 |
| 5 | `customer.sql` | 客户表（商机转客户的落库目标） |

若本机已有旧版 `qk` 库，可直接迁移数据：

```bash
mysqldump -uroot -p --set-gtid-purged=OFF qk > backup.sql
mysql -uroot -p -e "CREATE DATABASE sharp_crm DEFAULT CHARACTER SET utf8mb4"
mysql -uroot -p sharp_crm < backup.sql
```

### 4.3 配置

`backend/sharp-crm-management/src/main/resources/application.yml` 中所有敏感项均为「环境变量 + 本地默认值」形式：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/sharp_crm}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
token:
  expire: 86400000
  signature: ${JWT_SIGNATURE:sharp-crm-secret}
```

| 环境变量 | 说明 |
|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 数据库连接 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 |
| `JWT_SIGNATURE` | JWT 签名密钥（生产必须覆盖） |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS 凭证（上传功能需要） |

### 4.4 启动后端

```bash
cd backend

# 编译 + 全部单元测试
mvn clean test

# 打包并运行
mvn clean package -DskipTests
java -jar sharp-crm-management/target/sharp-crm-management-1.0-SNAPSHOT.jar
```

- 接口根路径：`http://localhost:8080`
- 接口文档（Knife4j）：`http://localhost:8080/doc.html`

### 4.5 启动前端

```bash
cd frontend
npm install
npm run dev     # 开发模式 http://localhost:5173，/api 自动代理到 8080
npm run build   # 生产构建，产物在 dist/
```

### 4.6 Nginx 部署（生产形态）

```bash
cd frontend && npm run build
cp -r dist/* ../ops/nginx/html/
cd ../ops/nginx && ./nginx.exe      # 监听 90 端口
```

`conf/nginx.conf` 已配置：静态资源 fallback 到 `index.html`（SPA 路由），`/api` 去前缀反代到 `http://localhost:8080`。

访问：`http://localhost:90`

### 4.7 Docker 一键部署

```bash
# 在仓库根目录执行，编排文件位于 ops/ 下，构建上下文指向根目录
docker compose -f ops/docker-compose.yml up -d --build
```

编排含 4 个服务：MySQL 8（挂载 `ops/sql/` 初始化）、Redis 7、Spring Boot 后端、Nginx 前端。后端使用 `docker` profile，数据库与 Redis 地址指向服务名。

## 五、功能模块

| 模块 | 功能 |
|---|---|
| 登录鉴权 | 账号密码登录（MD5 校验）、JWT 签发、注册、图形验证码 |
| 权限控制 | 接口级角色校验（`@RequireRole` + AOP）、数据权限隔离（管理员看全部，专员只看名下）、前端菜单按角色收敛 |
| 仪表盘 | 概览指标、近 6 个月趋势、线索来源与状态分布、转化漏斗，全部由数据库实时聚合 |
| 待办中心 | 聚合商机跟进到期、线索回收预警、合同回款到期三类事项，按紧急度排序，工作台显示摘要 |
| 市场活动 | 分页查询、按类型查询、增删改 |
| 线索管理 | 分页、线索池、分配、跟进、标记伪线索、转商机、Excel 导入导出 |
| 商机管理 | 分页、公海池、分配、跟进、踢回公海、**转客户** |
| 客户管理 | 成交客户分页查询、条件检索、**客户 360 视图** |
| 客户 360 | 线索 → 商机 → 成交 → 合同 → 回款全链路时间线，含转化周期、跟进次数、回款率 |
| 合同回款 | 合同签订、按付款方式自动生成分期计划、回款登记、逾期标记、回款进度统计 |
| 课程管理 | 分页、按学科查询、全量列表、增删改（带 Redis 缓存） |
| 用户 / 部门 / 角色 | 标准 CRUD、按角色或部门检索、删除前的引用校验 |
| 文件上传 | 图片上传至 OSS，返回访问 URL |
| 操作日志 | AOP 自动记录写操作，异步落库，支持分页查询 |

## 六、接口概览

除 `/login`、`/register`、`/captcha` 外均需请求头携带 `token`。完整文档见 Knife4j。

| 前缀 | 主要接口 |
|---|---|
| `/login` | `POST /login` 登录 |
| `/register` | `POST /register` 注册 |
| `/captcha` | `GET /captcha` 图形验证码 |
| `/dashboard` | `GET /stats` 看板统计（概览 + 趋势 + 分布 + 漏斗）、`GET /todos` 我的待办 |
| `/users` | 分页、详情、新增、修改、批量删除、按角色/部门查询 |
| `/clues` | 分页、线索池、详情、新增、分配、跟进、伪线索、转商机、`POST /import`、`GET /export` |
| `/businesses` | 分页、公海池、详情、新增、分配、跟进、踢回公海、转客户 |
| `/customers` | 分页、详情、全量列表、`GET /{id}/360` 客户 360 视图 |
| `/contracts` | 分页、详情、签订、修改、删除、`PUT /payment` 回款登记 |
| `/courses` | 分页、按学科、全量列表、增删改 |
| `/activities` | 分页、按类型、详情、增删改 |
| `/depts` `/roles` | 分页、全量列表、详情、增删改 |
| `/logs` | 操作日志分页 |
| `/upload` | `POST /upload` 图片上传，返回 URL |
| `/actuator` | `GET /health` 健康检查、`GET /info` 应用信息（免登录，供容器探针） |

统一返回结构：

```json
{ "code": 1, "msg": "success", "data": {} }
```

分页数据：`{ "total": 100, "rows": [] }`；`code = 1` 成功，`code = 0` 失败。

## 七、设计要点

- **无状态鉴权**：登录签发 JWT（载荷含 `id`、`username`、`name`、`image`、`roleId`、`roleLabel`），`LoginInterceptor` 校验请求头 `token`，失败返回 401；`afterCompletion` 清理 ThreadLocal 防泄漏。
- **接口级鉴权**：`@RequireRole` 注解 + `RoleAuthAspect` 切面，在方法执行前比对 JWT 中的角色标识。与拦截器分工明确——拦截器判断「有没有登录」，切面判断「这个角色能不能做这件事」。管理类接口（用户、部门、角色、日志）仅 `admin` 可写，线索/商机分配分别限定给对应专员角色。
- **数据权限**：`DataScopeHelper` 按角色推导查询范围，管理员返回 `null` 表示不限，其他角色强制为本人 id。范围值由服务端推导并覆盖 DTO 字段，前端即便手工传参也无法绕过；详情接口额外做归属校验，避免直接改 URL 拿到他人数据。
- **缓存降级**：`RedisCache` 对读写异常统一捕获并回退到本地 `ConcurrentHashMap`，Redis 不可用时业务不中断，仅失去跨实例一致性。
- **缓存一致性**：课程全量列表走缓存，新增/修改/删除后主动清理，避免脏读。
- **异步审计**：`LogAspect` 在主线程取好操作人再交给独立 `AsyncLogService`（`@Async` 必须跨 Bean 才生效），线程池拒绝策略为 `CallerRunsPolicy`，保证日志不丢。
- **定时回收**：`ClueRecycleTask` 按 cron 扫描长期未跟进的线索，自动踢回公海，解决线索呆滞。
- **统一异常**：`GlobalException` 用 `@RestControllerAdvice` 区分业务异常（回具体消息）与系统异常（回「系统异常!」，不外泄堆栈）。
- **字段自动填充**：`MyMetaObjectHandler` 维护 `create_time` / `update_time`。
- **前端封装**：Axios 拦截器统一注入 token、统一处理 401 与错误提示；`SimpleCrud` 组件由配置驱动，5 个管理页复用同一套表格逻辑。
- **事务边界**：跨表写操作（线索转商机、商机转客户、跟进记录落库、合同签订与回款登记）统一加 `@Transactional(rollbackFor = Exception.class)`。此前只有 3 处事务，业务中途抛异常会留下「线索已结束但没有商机」的断链数据。
- **乐观锁**：`clue` / `business` / `contract` 表增加 `version` 字段，配合 `OptimisticLockerInnerInterceptor`。前端带回旧版本号时更新语句自动追加 `version` 条件，两个销售同时跟进同一条线索，后提交的一方更新失败而不是静默覆盖；不传版本号则不加锁，兼容既有调用方。
- **参数校验**：实体字段加 `@NotBlank` / `@Pattern` / `@Size`，用 `Create` / `Update` 分组区分「新增必填、更新可缺省」，`GlobalException` 捕获 `MethodArgumentNotValidException` 返回字段级错误（如「phone：手机号格式不正确」），前端可直接定位到具体输入框。
- **链路追踪**：`TraceIdFilter` 为每个请求生成 16 位 traceId 写入 MDC，日志格式输出为 `[traceId]`，同时回写响应头 `X-Trace-Id`；调用方也可透传该头实现跨系统串联。排查线上问题时拿这个 id grep 日志即可捞出整条请求链路。
- **慢 SQL 监控**：`SlowSqlInterceptor` 拦截 `StatementHandler` 统计执行耗时，超过 500ms 打 warn 并输出压平后的 SQL，日志自带 traceId，能直接定位是哪次请求产生的慢查询。
- **分期金额尾差**：合同分期采用「前 n-1 期向下取整均分、最后一期承担尾差」，保证各期之和严格等于合同总额（10000 分 3 期 → 3333.33 + 3333.33 + 3333.34）。
- **前端产物拆分**：Vite `manualChunks` 按「框架 / 组件库 / 图表库」拆包，业务代码改动时第三方大块的 hash 不变、浏览器缓存直接命中；ECharts 改为按需引入，产物从 1036KB 降到 529KB（gzip 343KB → 178KB）。

## 八、测试账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| `zhangsan` | `zhangsan123` | 管理员 |
| `lisi` | `lisi123` | 线索专员 |

密码以 `MD5(明文密码)` 存储（无盐）。种子账号的初始密码约定为「用户名 + 123」，如 `zhangsan` 的初始密码是 `zhangsan123`。

## 九、已知限制

- **权限模型为角色级**：实现了「角色 → 接口」的粗粒度控制，未做到菜单/按钮级权限点与角色动态绑定；新增角色时需在代码中补充 `@RequireRole` 声明的标识。
- **存量客户缺少归属人**：7 条历史客户由旧库迁移而来，`user_id` 为空且来源商机已不存在，因此专员账号下不可见、管理员可见；新转化的客户会自动继承商机归属人。
- **OSS 凭证为示例值**：`application.yml` 中 bucket 与 endpoint 保留原示例配置，上传功能需替换为自有 bucket 并配置环境变量。
- **Redis 为可选依赖**：单机部署无 Redis 时走本地缓存，多实例部署必须接入 Redis 才能保证缓存一致。
- **单元测试聚焦于纯逻辑**：30 个测试覆盖 JWT、结果封装、ThreadLocal、登录校验、线索转商机、数据权限推导、合同分期拆单与回款状态流转；未做 Controller 层集成测试（依赖真实库）。
- **合同变更未做版本化**：已有回款记录的合同不允许修改金额，避免回款计划与合同金额对不上；真实业务中的「合同变更」需要另立变更单，本项目未实现。
- **Element Plus 为全量引入**：产物中 element 块约 1.1MB（gzip 345KB），是当前体积的主要构成，进一步压缩需引入按需加载插件。

## 十、性能表现

压测脚本 `scripts/bench.py`（Python 标准库，无额外依赖），混合请求四个高频读接口。

| 并发 | 总请求 | QPS | 平均延迟 | P95 | P99 | 错误率 |
|---|---|---|---|---|---|---|
| 20 | 1000 | 281.9 | 69.4 ms | 191.1 ms | 317.1 ms | 0% |
| 50 | 2000 | 359.7 | 131.6 ms | 398.0 ms | 543.3 ms | 0% |
| 50（关闭 SQL 日志） | 2000 | **459.1** | 108.2 ms | 250.2 ms | 579.5 ms | 0% |

关键结论：SQL 控制台同步打印是最大的非业务开销，切到 `NoLoggingImpl` 后 QPS 提升 27.6%、P95 下降 37%。完整分析（连接池长尾、看板聚合优化方向）见 [`docs/压测报告.md`](docs/压测报告.md)。

复现：

```bash
python scripts/bench.py -c 50 -n 40
```

## 十一、工程化

- **CI**：`.github/workflows/ci.yml`，push / PR 触发两条流水线——后端 `mvn clean test` + 打包，前端 `npm ci` + 构建，产物留存 7 天。
- **健康检查**：`GET /actuator/health` 免登录，供容器探针使用；Redis 作为可选依赖不参与整体健康判定，避免无 Redis 环境被误判为不可用。
- **配置外置**：数据库、Redis、JWT 密钥、MyBatis 日志级别均支持环境变量覆盖，镜像不需要重新打包即可切换环境。
- **数据库脚本**：`ops/sql/` 下按表拆分建表语句，`contract.sql` 为本次新增的合同与回款两张表。

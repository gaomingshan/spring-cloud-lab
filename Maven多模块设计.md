# Spring Cloud Lab - Maven 多模块项目设计

> 基于《公司级分布式系统技术基线（中台标准模板）》设计，以**学习微服务治理能力**为核心目标。
> 每个治理能力独立成模块，互不耦合，可单独启动演示。

---

## 一、整体目录结构

```
spring-cloud-lab/                            ← 根 POM（纯 BOM，无业务代码）
├── pom.xml                                  ← 父 POM：统一版本管理 + 插件管理
│
├── lab-common/                             ← 公共基础层（所有模块共享）
│   ├── pom.xml
│   └── src/main/java/com/lab/common/
│       ├── result/                          ← 统一响应模型 Result<T>
│       ├── exception/                       ← 统一异常体系 + 错误码
│       ├── util/                            ← 工具类（ID生成、加密等）
│       └── constant/                        ← 公共常量
│
├── lab-config-center/                      ← 【配置中心】Nacos Config
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-registry/                           ← 【注册中心】Nacos Discovery
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── provider/                        ← 服务提供方（多实例演示）
│       └── consumer/                        ← 服务消费方
│
├── lab-gateway/                            ← 【API 网关】Spring Cloud Gateway
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-feign/                              ← 【服务调用 + 负载均衡】OpenFeign + LoadBalancer
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── provider/                        ← 服务提供方（3实例演示负载均衡）
│       └── consumer/                        ← 服务消费方（Feign 调用）
│
├── lab-sentinel/                           ← 【流控熔断】Sentinel
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-seata/                              ← 【分布式事务】Seata AT/TCC
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── seata-order/                     ← 订单服务（事务发起方）
│       ├── seata-account/                   ← 账户服务（事务参与方）
│       └── seata-storage/                   ← 库存服务（事务参与方）
│
├── lab-stream/                             ← 【消息队列】Spring Cloud Stream
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── producer/                        ← 消息生产者
│       └── consumer/                        ← 消息消费者
│
├── lab-sharding/                           ← 【分库分表】ShardingSphere  ← 新增
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-multi-datasource/                   ← 【异构数据源】MySQL + MongoDB  ← 新增
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-observability/                      ← 【可观测性】三大支柱：Metrics + Tracing + Logging
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
├── lab-xxljob/                             ← 【任务调度】XXL-Job
│   ├── pom.xml
│   ├── README.md
│   └── src/
│
└── lab-security/                           ← 【安全认证】Sa-Token
    ├── pom.xml
    ├── README.md
    └── src/
        ├── auth-server/                     ← 认证服务（登录、Token 颁发）
        └── resource-server/                 ← 资源服务（Token 校验演示）
```

---

## 二、模块划分总览（13个模块）

| # | 模块名 | 治理能力 | 核心技术 | 外部依赖 |
|---|--------|---------|---------|----------|
| 1 | `lab-common` | 公共基础能力 | Lombok、Jackson、Hibernate Validator | 无 |
| 2 | `lab-config-center` | 配置中心 | Nacos Config | Nacos Server |
| 3 | `lab-registry` | 注册中心 | Nacos Discovery | Nacos Server |
| 4 | `lab-gateway` | API 网关（路由/限流/灰度/安全） | Spring Cloud Gateway、Sa-Token、Redis | Nacos、Redis |
| 5 | `lab-feign` | 服务调用 + 负载均衡 | OpenFeign、OkHttp、LoadBalancer | Nacos |
| 6 | `lab-sentinel` | 流控熔断 | Sentinel、Sentinel Dashboard | Sentinel Dashboard |
| 7 | `lab-seata` | 分布式事务 | Seata AT/TCC、MySQL | Seata Server、MySQL、Nacos |
| 8 | `lab-stream` | 消息队列 | Spring Cloud Stream（RocketMQ Binder） | RocketMQ |
| 9 | `lab-sharding` | 分库分表 | ShardingSphere 5.x、MySQL | MySQL（多实例） |
| 10 | `lab-multi-datasource` | 异构数据源 | MyBatis-Flex + Spring Data MongoDB | MySQL、MongoDB |
| 11 | `lab-observability` | 可观测性（Metrics + Tracing + Logging） | Micrometer/Prometheus/Grafana + SkyWalking + ELK | Prometheus、Grafana、SkyWalking OAP、Elasticsearch |
| 12 | `lab-xxljob` | 分布式任务调度 | XXL-Job | XXL-Job Admin |
| 13 | `lab-security` | 认证鉴权 | Sa-Token、jBCrypt | Redis |

---

## 三、根 POM 核心设计

### 3.1 坐标与子模块声明

```xml
<groupId>com.lab</groupId>
<artifactId>spring-cloud-lab</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>

<modules>
    <module>lab-common</module>
    <module>lab-config-center</module>
    <module>lab-registry</module>
    <module>lab-gateway</module>
    <module>lab-feign</module>
    <module>lab-sentinel</module>
    <module>lab-seata</module>
    <module>lab-stream</module>
    <module>lab-sharding</module>
    <module>lab-multi-datasource</module>
    <module>lab-observability</module>
    <module>lab-xxljob</module>
    <module>lab-security</module>
</modules>
```

### 3.2 版本属性（properties）

```xml
<properties>
    <!-- 运行环境 -->
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    <!-- Spring 全家桶 BOM -->
    <spring-boot.version>3.2.5</spring-boot.version>
    <spring-cloud.version>2023.0.1</spring-cloud.version>
    <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>

    <!-- 数据层 -->
    <mybatis-flex.version>1.9.3</mybatis-flex.version>
    <shardingsphere.version>5.4.1</shardingsphere.version>
    <redisson.version>3.29.0</redisson.version>

    <!-- 流控熔断 -->
    <sentinel.version>1.8.8</sentinel.version>

    <!-- 分布式事务 -->
    <seata.version>2.0.0</seata.version>

    <!-- 任务调度 -->
    <xxl-job.version>2.4.1</xxl-job.version>

    <!-- 认证鉴权 -->
    <sa-token.version>1.38.0</sa-token.version>
    <jbcrypt.version>0.4</jbcrypt.version>

    <!-- 可观测性 -->
    <skywalking.version>9.2.0</skywalking.version>
    <micrometer.version>1.12.5</micrometer.version>
    <micrometer-tracing.version>1.2.5</micrometer-tracing.version>

    <!-- 工具库 -->
    <lombok.version>1.18.32</lombok.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <easyexcel.version>3.3.4</easyexcel.version>
    <transmittable-thread-local.version>2.14.5</transmittable-thread-local.version>
    <owasp-encoder.version>1.3.1</owasp-encoder.version>

    <!-- Maven 插件 -->
    <maven-compiler-plugin.version>3.13.0</maven-compiler-plugin.version>
    <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
</properties>
```

### 3.3 dependencyManagement（BOM 统一版本锁定）

```xml
<dependencyManagement>
    <dependencies>
        <!-- ① Spring Boot BOM -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <!-- ② Spring Cloud BOM -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <!-- ③ Spring Cloud Alibaba BOM -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>${spring-cloud-alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- ④ 项目公共模块 -->
        <dependency>
            <groupId>com.lab</groupId>
            <artifactId>lab-common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- ⑤ 数据层 -->
        <dependency>
            <groupId>com.mybatis-flex</groupId>
            <artifactId>mybatis-flex-spring-boot3-starter</artifactId>
            <version>${mybatis-flex.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
        </dependency>

        <!-- ⑥ 流控熔断 -->
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-core</artifactId>
            <version>${sentinel.version}</version>
        </dependency>

        <!-- ⑦ 分布式事务 -->
        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
            <version>${seata.version}</version>
        </dependency>

        <!-- ⑧ 任务调度 -->
        <dependency>
            <groupId>com.xuxueli</groupId>
            <artifactId>xxl-job-core</artifactId>
            <version>${xxl-job.version}</version>
        </dependency>

        <!-- ⑨ 认证鉴权 -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-reactor-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- ⑩ 工具库 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>${easyexcel.version}</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>transmittable-thread-local</artifactId>
            <version>${transmittable-thread-local.version}</version>
        </dependency>
        <dependency>
            <groupId>org.owasp.encoder</groupId>
            <artifactId>encoder</artifactId>
            <version>${owasp-encoder.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>${jbcrypt.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> **核心原则**：根 POM 只做版本锁定，**不声明实际 `<dependencies>`**。各子模块按需引入，最小依赖原则。

### 3.4 pluginManagement（插件统一管理）

```xml
<build>
    <pluginManagement>
        <plugins>
            <!-- 编译插件：Lombok + MapStruct 联合注解处理 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <!-- Spring Boot 可执行 JAR 打包 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <!-- 单元测试 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${maven-surefire-plugin.version}</version>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

---

## 四、各模块职责与学习重点

### 4.1 lab-common（公共基础层）
- **职责**：所有子模块的公共依赖，不可单独运行
- **包含**：`Result<T>` 统一响应、`BizException` 统一异常、错误码枚举、`TraceId` 工具类
- **注意**：不包含 `@SpringBootApplication`，不依赖任何业务模块

### 4.2 lab-config-center（配置中心）
- **学习重点**：Nacos Config 动态刷新、多环境（dev/test/prod）、共享配置（shared-configs）、灰度发布
- **核心注解**：`@RefreshScope`、`@Value`、`@ConfigurationProperties`
- **README 重点**：`bootstrap.yml` 完整配置详解、Nacos 控制台操作步骤、动态刷新原理
- **外部依赖**：Nacos Server 2.x

### 4.3 lab-registry（注册中心）
- **学习重点**：服务注册、心跳续约、服务发现、健康检查、权重调整、优雅下线
- **演示结构**：provider × 2实例（模拟集群）+ consumer（RestTemplate/DiscoveryClient 调用）
- **README 重点**：实例权重、元数据配置、Nacos 控制台 UI 说明
- **外部依赖**：Nacos Server 2.x

### 4.4 lab-gateway（API 网关）
- **学习重点**：动态路由、断言工厂（Predicate）、过滤器链（GlobalFilter/GatewayFilter）、限流（Redis 令牌桶）、灰度路由（Header/Weight）、Sa-Token 统一鉴权、API 版本控制（/v1/ /v2/）、签名校验、防重放攻击
- **注意**：Gateway 基于 WebFlux，**不兼容 Spring MVC Servlet**，Sa-Token 需使用 Reactor 版
- **外部依赖**：Nacos（动态路由）、Redis（限流令牌桶）

### 4.5 lab-feign（服务调用 + 负载均衡）
- **学习重点**：OpenFeign + OkHttp 声明式调用、LoadBalancer 负载均衡策略（轮询/随机/加权）、Feign 日志级别、超时配置、`RequestInterceptor` 统一请求头传递（TraceId/Token）
- **演示结构**：provider × 3实例 + consumer，观察负载均衡分发效果
- **README 重点**：`@FeignClient` 详解、Fallback 降级配置、OkHttp 替换原生 HttpClient 原因
- **外部依赖**：Nacos

### 4.6 lab-sentinel（流控熔断）
- **学习重点**：QPS 流控、线程数流控、熔断规则（慢调用比例/异常比例/异常数）、热点参数限流、系统自适应保护、Sentinel Dashboard 实时监控
- **README 重点**：规则持久化到 Nacos、`@SentinelResource` 注解使用、与 Spring Cloud Gateway 集成
- **外部依赖**：Sentinel Dashboard

### 4.7 lab-seata（分布式事务）
- **学习重点**：AT 模式（自动补偿，undo_log）、TCC 模式（手动 Try/Confirm/Cancel）、全局事务 `@GlobalTransactional`
- **演示结构**：seata-order（发起方）→ seata-account（扣款）→ seata-storage（扣库存），模拟电商下单场景
- **README 重点**：AT vs TCC 适用场景对比、undo_log 表结构、Seata Server 配置、Nacos 作为 TC 注册中心
- **外部依赖**：Seata Server 2.x、MySQL、Nacos

### 4.8 lab-stream（消息队列）
- **学习重点**：Spring Cloud Stream 抽象层（Binder/Binding/Channel）、RocketMQ 顺序消息、延迟消息、事务消息、消费幂等、死信队列
- **演示结构**：producer + consumer，演示发布/订阅、消息重试、DLQ 处理
- **README 重点**：Stream 抽象如何屏蔽 MQ 实现差异、`@StreamListener` vs 函数式编程模型、RocketMQ Binder 配置详解
- **外部依赖**：RocketMQ Broker + NameServer

### 4.9 lab-sharding（分库分表）⭐ 新增
- **学习重点**：ShardingSphere JDBC 模式分库分表规则、水平分片（Hash/Range）、垂直分表、读写分离、分布式主键（Snowflake）
- **演示场景**：订单表按 user_id 分 2库×4表（共8张），演示路由规则
- **README 重点**：分片算法（精确分片/范围分片/复合分片）、ShardingSphere 配置 YAML 详解、与 MyBatis-Flex 集成注意事项、分页查询特殊处理
- **外部依赖**：MySQL × 2实例（或同实例不同 schema 模拟）

### 4.10 lab-multi-datasource（异构数据源）⭐ 新增
- **学习重点**：同一服务同时操作 MySQL（关系型）和 MongoDB（文档型）异构数据库、MyBatis-Flex 动态数据源切换、`@DS` 注解路由、Spring Data MongoDB 集成
- **演示场景**：用户基础信息存 MySQL（强一致、事务），用户行为日志/动态内容存 MongoDB（高写入、灵活 Schema）
- **README 重点**：多数据源配置原理、事务边界处理（异构数据源事务不可跨库）、MyBatis-Flex 与 Spring Data MongoDB 共存配置
- **外部依赖**：MySQL、MongoDB

### 4.11 lab-observability（可观测性 — Metrics + Tracing + Logging）

> 可观测性三大支柱各司其职，通过 **TraceId** 串联，形成完整的问题定位闭环。

#### Metrics（指标监控）
- **技术栈**：Micrometer → Prometheus 采集 → Grafana 可视化
- **学习重点**：`/actuator/prometheus` 端点暴露、自定义业务指标（Counter/Gauge/Timer）、Grafana 监控大盘搭建

#### Tracing（链路追踪）
- **技术栈**：SkyWalking Java Agent（字节码注入，**无侵入**）→ SkyWalking OAP → SkyWalking UI
- **学习重点**：Agent 配置、跨服务 TraceId 透传、慢接口/错误接口定位、与 ELK 通过 TraceId 打通
- **注意**：SkyWalking Agent 以 `-javaagent` 方式启动，**不需要在代码中引入任何依赖**

#### Logging（日志体系）
- **技术栈**：Logback → 结构化 JSON 日志 → Filebeat 采集 → Logstash 处理 → Elasticsearch 存储 → Kibana 检索
- **学习重点**：MDC 注入 TraceId/UserId、`logstash-logback-encoder` JSON 格式输出、统一日志规范（何时打 INFO/WARN/ERROR）
- **TraceId 贯通**：SkyWalking Agent 自动将 TraceId 写入 MDC，Logback pattern 引用 `%X{tid}`，实现「一个 TraceId 串联链路图 + 日志行」

```
业务请求进入
  │
  ├─ SkyWalking Agent（字节码增强）
  │     └─ 生成 TraceId，注入 MDC
  │     └─ 上报 Span → SkyWalking OAP → SkyWalking UI（链路图）
  │
  └─ Logback（MDC 取 TraceId 打印 JSON 日志）
        └─ Filebeat 采集日志文件
              └─ Logstash 解析 → Elasticsearch
                    └─ Kibana 按 TraceId 检索日志
```

- **外部依赖**：SkyWalking OAP、Prometheus、Grafana、Elasticsearch、Kibana

### 4.12 lab-xxljob（分布式任务调度）
- **学习重点**：XXL-Job 执行器注册、任务 Handler（Bean 模式/GLUE 模式）、分片广播任务、失败重试、任务告警
- **README 重点**：执行器与 Admin 通信机制、分片任务如何实现并行处理海量数据
- **外部依赖**：XXL-Job Admin

### 4.13 lab-security（认证鉴权）
- **学习重点**：Sa-Token 登录认证、多端 Token（PC/App/小程序）、权限认证（`@SaCheckPermission`）、单点登录（SSO）、Token 持久化到 Redis
- **演示结构**：auth-server（登录/注销/刷新 Token）+ resource-server（接口权限校验）
- **README 重点**：Sa-Token 与 Gateway 集成（网关统一鉴权 vs 服务内鉴权）、jBCrypt 密码加密最佳实践
- **外部依赖**：Redis

---

## 五、模块依赖关系图

```
┌──────────────────────────────────────────────────────┐
│                  根 POM（BOM 版本管理）                │
└──────────────────────────┬───────────────────────────┘
                           │ 所有模块继承
          ┌────────────────┼─────────────────┐
          ▼                ▼                 ▼
     lab-common        lab-*模块          lab-*模块
    （公共基础层）      依赖 common        依赖 common
```

**依赖原则**：
- `lab-common` 只能被其他模块依赖，反向禁止
- 各治理模块之间**互不依赖**，学习时可单独启动
- `lab-seata` / `lab-multi-datasource` 等内部多服务通过 **独立 Spring Boot 进程** 模拟微服务间调用

---

## 六、外部基础设施启动清单

| 基础设施 | 涉及模块 | 推荐启动方式 |
|---------|---------|-------------|
| Nacos Server 2.x | config/registry/gateway/feign/sentinel/seata | Docker Compose |
| MySQL 8.0 | seata/sharding/multi-datasource | Docker Compose |
| MongoDB | multi-datasource | Docker |
| Redis 7.x | gateway/security | Docker |
| Sentinel Dashboard | sentinel | 官方 JAR 直接运行 |
| Seata Server 2.x | seata | Docker / 官方包 |
| RocketMQ | stream | Docker Compose |
| SkyWalking OAP + UI | observability | Docker Compose |
| Prometheus + Grafana | observability | Docker Compose |
| Elasticsearch + Kibana | observability | Docker Compose |
| XXL-Job Admin | xxljob | Docker / 官方包 |

> 建议根目录提供 `docker-compose.yml`，一键拉起所有基础设施，各模块只需关注业务代码。 
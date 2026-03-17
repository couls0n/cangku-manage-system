# Warehouse Management

一个经过安全增强的 Spring Boot 仓库管理后端，默认使用 H2 启动，支持切换到 MySQL，并内置 API 频控、水平/垂直越权防御，以及 eBPF 异常事件接入与后台监控接口。

## 目录结构

```text
warehouse-management
├─ ops/ebpf/                    Linux 侧 eBPF 采集与转发脚本
├─ src/main/java/com/warehouse
│  ├─ common/                   通用返回体与异常处理
│  ├─ config/                   MyBatis、Web、安全配置
│  ├─ controller/               业务、认证、监控接口
│  ├─ entity/                   业务实体
│  ├─ mapper/                   业务 Mapper
│  ├─ monitoring/               eBPF 事件、告警、监控服务
│  ├─ security/                 Token、限流、鉴权与访问控制
│  ├─ service/                  业务服务
│  └─ WarehouseApplication.java 启动入口
├─ src/main/resources
│  ├─ application.yml           默认 H2 配置
│  ├─ application-mysql.yml     MySQL 配置
│  ├─ schema.sql                启动建表脚本
│  ├─ data.sql                  初始化数据
│  └─ db/init.sql               MySQL 使用说明
└─ src/test/java/com/warehouse  启动与安全链路测试
```

## 启动方式

默认启动：

```bash
mvn spring-boot:run
```

指定端口：

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

切换 MySQL：

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

默认账号：

- 管理员：`admin / 123456`
- 仓库操作员：`operator / 123456`

## 安全能力

- API 频控：对全部 `/api/**` 生效，登录接口更严格。
- 垂直越权防御：管理员接口必须管理员角色访问。
- 水平越权防御：普通操作员仅可访问自己所属仓库的数据，用户资料仅可访问本人。
- 安全告警：越权拦截和 eBPF 异常事件会进入 `security_alert`。

## 监控接口

- `POST /api/security/ebpf/ingest`
  - Header: `X-EBPF-KEY: warehouse-ebpf-agent-key`
- `GET /api/security/dashboard`
- `GET /api/security/events`
- `GET /api/security/alerts`

## eBPF 接入

Linux 主机上可使用：

```bash
chmod +x ops/ebpf/forward-events.sh
EBPF_INGEST_KEY=warehouse-ebpf-agent-key ./ops/ebpf/forward-events.sh http://127.0.0.1:8081/api/security/ebpf/ingest
```

`warehouse-guard.bt` 会实时采集 Java 进程的 `execve`、`openat`、`connect` 事件，并转发到本项目后台。

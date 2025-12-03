# OpenTelemetry Java Extension

这是一个 OpenTelemetry Java 扩展项目，用于扩展 OpenTelemetry Java Agent 的功能。项目通过自定义采样器和扩展配置来增强对分布式追踪、指标和日志的支持。

## 功能特性

- **自定义采样器**：集成 CEL 基础采样器，支持灵活的分布式追踪采样策略
- **自动检测和过滤**：支持健康检查和执行器端点的自动过滤
- **多种信号支持**：支持追踪（Traces）、指标（Metrics）和日志（Logs）导出
- **OpenTelemetry SDK 自动配置**：基于 YAML 配置文件进行灵活配置
- **OTLP HTTP 导出**：通过 OTLP HTTP 协议导出数据到可观测性平台

## 快速开始

### 前置要求

- Java 8 或更高版本
- Gradle 7.0 或更高版本
- OpenTelemetry Collector 或兼容的可观测性平台

### 构建项目

使用 Gradle 构建项目：

```bash
./gradlew build
```

构建完成后，扩展的 Java Agent JAR 将生成在 `build/libs/` 目录：

```bash
build/libs/opentelemetry-javaagent.jar
```

### 使用 Java Agent

1. **基础使用**：

```bash
java -javaagent:./build/libs/opentelemetry-javaagent.jar \
     -Dotel.service.name=my-service \
     -jar your-application.jar
```

2. **使用配置文件**：

将 `sdk-config.yaml` 复制到应用程序目录，设置环境变量：

```bash
export OTEL_SDK_DISABLED=false
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
export OTEL_SERVICE_NAME=my-service

java -javaagent:./build/libs/opentelemetry-javaagent.jar \
     -jar your-application.jar
```

3. **Docker 使用**：

```bash
docker build -t my-service:latest .

docker run -e OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318 \
           -e OTEL_SERVICE_NAME=my-service \
           my-service:latest
```

## 项目结构

```
opentelemetry-java-extension/
├── src/                          # 源代码目录
│   ├── main/java/               # 主程序代码
│   └── test/java/               # 测试代码
├── build/                        # 构建输出目录
│   └── libs/                     # 编译后的 JAR 文件
├── gradle/                       # Gradle 包装器目录
├── build.gradle                  # Gradle 构建配置文件
├── settings.gradle               # Gradle 项目设置文件
├── Dockerfile                    # Docker 镜像构建文件
├── sdk-config.yaml               # OpenTelemetry SDK 配置文件
├── gradlew                        # Linux/Mac Gradle 包装器脚本
├── gradlew.bat                   # Windows Gradle 包装器脚本
└── README.md                      # 本文件
```

## 配置说明

### sdk-config.yaml 配置文件

该文件定义了 OpenTelemetry SDK 的各项配置：

#### 1. 资源配置 (Resource)

```yaml
resource:
  attributes:
    - name: service.name
      value: ${OTEL_SERVICE_NAME:-unknown_service}
```

定义服务的基本属性，如服务名称。

#### 2. 采样器配置 (Sampler)

项目使用 CEL 基础采样器，支持条件表达式过滤：

```yaml
sampler:
  parent_based:
    root:
      cel_based:
        fallback_sampler:
          always_on:
        expressions:
          # 示例：丢弃健康检查端点的跟踪
          - action: DROP
            expression: spanKind == 'SERVER' && attribute['url.path'].startsWith('/health')
```

支持的操作：
- `DROP`：丢弃该跟踪
- `RECORD_AND_SAMPLE`：记录并采样该跟踪
- `RECORD_ONLY`：仅记录不采样

#### 3. 导出器配置 (Exporter)

支持通过 OTLP HTTP 导出追踪、指标和日志：

```yaml
tracer_provider:
  processors:
    - batch:
        exporter:
          otlp_http:
            endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:-http://localhost:4318}/v1/traces
```

#### 4. 环境变量

常用的环境变量配置：

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `OTEL_SERVICE_NAME` | `unknown_service` | 服务名称 |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | OTLP 导出端点 |
| `OTEL_LOG_LEVEL` | `info` | 日志级别 |
| `OTEL_SDK_DISABLED` | `false` | 是否禁用 SDK |
| `OTEL_PROPAGATORS` | `tracecontext,baggage` | 传播器列表 |
| `OTEL_BSP_SCHEDULE_DELAY` | `5000` | 批处理调度延迟（毫秒） |

## 依赖

项目主要依赖：

- **OpenTelemetry Java Agent**: v2.22.0 - OpenTelemetry 官方 Java Agent
- **OpenTelemetry CEL Sampler**: v1.52.0-alpha - CEL 基础采样器
- **Jackson DataBind**: v2.20.0 - JSON 处理库
- **OkHttp**: v5.2.1 - HTTP 客户端库
- **JUnit 5**: v5.14.0 - 单元测试框架
- **Testcontainers**: v2.0.1 - 容器化测试支持

## 构建任务

### 主要 Gradle 任务

- `./gradlew build` - 构建项目并生成扩展 Java Agent
- `./gradlew test` - 运行单元测试
- `./gradlew clean` - 清理构建输出
- `./gradlew shadowJar` - 生成 Shadow JAR（包含所有依赖）
- `./gradlew extendedAgent` - 生成扩展的 Java Agent JAR

### 自定义任务

**extendedAgent** - 生成包含项目扩展的 OpenTelemetry Java Agent JAR

此任务将：
1. 下载官方 OpenTelemetry Java Agent
2. 将项目编译的 Shadow JAR 打包到 `extensions` 目录中
3. 保留官方 Agent 的 MANIFEST.MF 文件
4. 输出为 `opentelemetry-javaagent.jar`


## Docker 支持

### 构建 Docker 镜像

```bash
docker build -t my-service:latest .
```

Dockerfile 包含：
- 将编译的 Java Agent 复制到 `/opt/otel/`
- 复制 SDK 配置文件
- 设置适当的文件权限

### 运行 Docker 容器

```bash
docker run -e OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318 \
           -e OTEL_SERVICE_NAME=my-service \
           my-service:latest
```

## 常见问题

### Q: 如何验证 Java Agent 是否正确加载？

A: 启动应用时添加日志级别设置：

```bash
java -javaagent:./build/libs/opentelemetry-javaagent.jar \
     -Dotel.log.level=debug \
     -jar your-application.jar
```

查看日志输出中是否包含 OpenTelemetry 初始化信息。

### Q: 如何自定义采样规则？

A: 编辑 `sdk-config.yaml` 文件中的 `sampler.parent_based.root.cel_based.expressions` 部分，添加 CEL 表达式来定义采样规则。

### Q: 支持哪些 OpenTelemetry 导出端点？

A: 项目配置支持 OTLP HTTP 导出协议。可导出到：
- OpenTelemetry Collector
- Jaeger（支持 OTLP 端点）
- 其他兼容 OTLP HTTP 的平台（如 Datadog、New Relic 等）

### Q: Java 8 支持情况如何？

A: 项目编译目标设置为 Java 8（`options.release.set(8)`），确保兼容 Java 8 及更高版本。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交前检查

1. 确保代码通过所有测试：`./gradlew test`
2. 遵循 Java 编码规范
3. 在 PR 中清晰描述变更内容
4. 为新功能添加相应的测试用例

## 许可证

该项目遵循 Apache License 2.0（或根据实际情况修改）。

## 相关资源

- [OpenTelemetry 官方网站](https://opentelemetry.io/)
- [OpenTelemetry Java 文档](https://opentelemetry.io/docs/instrumentation/java/)
- [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
- [OpenTelemetry Java Contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)
- [CEL 采样器文档](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/cel-sampler)
- [OTLP 协议规范](https://opentelemetry.io/docs/specs/otlp/)

## 联系方式

如有问题或建议，请提交 Issue 或联系项目维护者。

---

**最后更新**: 2025年12月3日

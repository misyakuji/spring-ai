# Spring AI 2.0 智能 AI 平台

> 基于 Spring AI 2.0 构建的企业级 AI 能力平台，提供持久化记忆 Chat、MCP & Tools、RAG 知识库三大核心服务。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring AI | 2.0.0-M8 |
| PostgreSQL | 16 |
| Redis | 8 |
| Milvus | 2.4.x |
| Docker Compose | 2.x |

## 快速开始

### 1. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，填入你的 DeepSeek API Key
```

### 2. 启动基础设施

```bash
docker compose up -d postgres redis etcd minio milvus
```

### 3. 启动应用

```bash
# 本地运行
mvn spring-boot:run

# 或打包后运行
mvn package -DskipTests
java -jar target/spring-ai-1.0.0.jar
```

### 4. 访问服务

| 服务 | 地址 |
|------|------|
| 应用 API | http://localhost:8080 |
| Actuator Health | http://localhost:8080/actuator/health |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus |
| MinIO Console | http://localhost:9001 |
| Milvus Attu | http://localhost:9091 |

## API 接口

### Chat 服务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/chat/sessions` | 创建会话 |
| GET | `/api/v1/chat/sessions` | 会话列表 |
| DELETE | `/api/v1/chat/sessions/{id}` | 删除会话 |
| POST | `/api/v1/chat/completions` | 发送消息（SSE 流式） |
| GET | `/api/v1/chat/sessions/{id}/messages` | 历史消息 |

### 知识库

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/knowledge/bases` | 创建知识库 |
| GET | `/api/v1/knowledge/bases` | 知识库列表 |
| POST | `/api/v1/knowledge/bases/{id}/documents` | 上传文档 |
| GET | `/api/v1/knowledge/bases/{id}/documents` | 文档列表 |
| DELETE | `/api/v1/knowledge/documents/{id}` | 删除文档 |
| POST | `/api/v1/knowledge/search` | 向量检索 |
| POST | `/api/v1/rag/chat` | RAG 对话（SSE 流式） |

### Tools

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/tools` | 已注册工具列表 |
| GET | `/api/v1/mcp/servers` | MCP Server 状态 |

## 项目结构

```
src/main/java/com/misyakuji/ai/
├── config/          # 配置类
├── controller/      # REST 控制器
├── dto/             # 数据传输对象
├── entity/          # JPA 实体
├── exception/       # 全局异常处理
├── memory/          # ChatMemory 实现
├── repository/      # Spring Data 仓库
├── service/         # 业务逻辑层
└── tools/           # @Tool 工具类
```

## 内置工具

- `getWeather` - 查询城市天气
- `getCurrentDateTime` - 获取当前时间
- `calculate` - 数学计算
- `searchKnowledge` - 知识库语义检索

## Docker 部署

```bash
# 构建并启动所有服务（含应用）
docker compose up -d --build

# 仅启动基础设施
docker compose up -d postgres redis etcd minio milvus
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DEEPSEEK_API_KEY` | DeepSeek API Key | - |
| `DEEPSEEK_BASE_URL` | DeepSeek API 地址 | https://api.deepseek.com |
| `POSTGRES_PASSWORD` | PostgreSQL 密码 | ai_pass_2024 |
| `REDIS_PASSWORD` | Redis 密码 | redis_2024 |
| `MINIO_SECRET_KEY` | MinIO 密钥 | minioadmin |

## License

MIT

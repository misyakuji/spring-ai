# Spring AI 2.0 智能 AI 平台项目规划

> **项目版本：** v1.0  
> **技术栈主导：** Java 21 + Spring Boot 3.x + Spring AI 2.0  
> **向量数据库：** Milvus  
> **部署方式：** Docker Compose  

---

## 一、项目概述

### 1.1 背景与目标

本项目旨在基于 **Spring AI 2.0** 构建一套企业级 AI 能力平台，对外提供标准化的 AI 服务接口。平台核心能力包括：

- **持久化记忆 Chat 服务**：支持多轮对话上下文管理，历史消息持久化存储
- **MCP & Tools 服务**：基于 Model Context Protocol 实现工具动态注册与调用
- **RAG 知识库服务**：支持文档导入、智能切片、向量化、语义检索的完整知识库管道

### 1.2 功能总览

| 模块 | 核心功能 | 优先级 |
|-----|---------|--------|
| Chat 服务 | 多轮对话、持久化记忆、会话管理 | P0 |
| MCP & Tools | 工具注册、MCP Server、Function Calling | P0 |
| RAG 知识库 | 文档解析、切片、Embedding、向量检索 | P0 |
| 向量数据库 | Milvus 集成、Collection 管理 | P0 |
| 基础设施 | Docker Compose 一键部署 | P0 |
| 管理后台| 知识库管理、会话历史查看 | P1 |

---

## 二、技术选型

### 2.1 核心依赖

| 技术 | 版本       | 说明 |
|------|----------|------|
| Java | 25 LTS   | 支持虚拟线程，提升并发性能 |
| Spring Boot | 4.0.6    | 主框架 |
| Spring AI | 2.0.0-M8 | AI 核心能力框架 |
| Spring Data JPA | -        | ORM 持久层 |
| PostgreSQL | 18.4     | 关系型数据库（Chat记忆、元数据） |
| Redis | redis:8-alpine      | 会话缓存、热点数据 |
| Milvus | 2.4.x    | 向量数据库 |
| etcd | 3.5.x    | Milvus 元数据存储（随 Milvus 附带） |
| MinIO | Latest   | Milvus 对象存储（随 Milvus 附带） |
| Docker Compose | 2.x      | 容器编排 |

### 2.2 Spring AI 2.0 核心组件

| 组件 | 用途 |
|------|------|
| `ChatClient` | 统一对话入口，支持链式调用 |
| `ChatMemory` | 对话记忆抽象，内置 InMemory/JDBC 实现 |
| `VectorStore` | 向量存储抽象，对接 Milvus |
| `EmbeddingModel` | 向量化模型接口 |
| `DocumentReader` | 文档读取（PDF/Word/TXT/HTML） |
| `TextSplitter` | 文档切片（Token/Sentence/Recursive） |
| `QuestionAnswerAdvisor` | RAG 检索增强 Advisor |
| `@Tool` / `@ToolParam` | 工具函数注册注解 |
| `McpSyncClient` | MCP 协议客户端 |
| `ToolCallbackProvider` | 工具回调注册 |

### 2.3 LLM 接入（可配置）

项目通过 Spring AI 的 `ChatModel` 抽象解耦 LLM，支持多种后端：

- **DeepSeek**（GPT-4o、GPT-4-turbo）
- **Ollama**（本地离线部署，Qwen / LLaMA 等）
- **智谱 AI / 通义千问 / 月之暗面**（通过 DeepSeek 兼容接口）

---

## 三、系统整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Client / API                          │
│              (HTTP REST / SSE Streaming)                     │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Spring Boot Application                    │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  Chat 服务   │  │  MCP/Tools   │  │   RAG 知识库      │   │
│  │  Controller │  │  Controller  │  │   Controller     │   │
│  └──────┬──────┘  └──────┬───────┘  └────────┬─────────┘   │
│         │                │                    │             │
│  ┌──────▼──────┐  ┌──────▼───────┐  ┌────────▼─────────┐   │
│  │ ChatClient  │  │ Tool Registry│  │  RAG Pipeline    │   │
│  │ + Advisor   │  │ McpClient    │  │  (Read→Split→    │   │
│  │ + Memory    │  │ FunctionCall │  │   Embed→Store)   │   │
│  └──────┬──────┘  └──────┬───────┘  └────────┬─────────┘   │
│         │                │                    │             │
│  ┌──────▼────────────────▼────────────────────▼──────────┐  │
│  │                  Spring AI Core Layer                  │  │
│  │    ChatModel │ EmbeddingModel │ VectorStore            │  │
│  └──────┬───────────────────────────────────┬────────────┘  │
└─────────┼───────────────────────────────────┼───────────────┘
          │                                   │
┌─────────▼──────────┐             ┌──────────▼──────────────┐
│   LLM Provider     │             │   Milvus Vector DB      │
│  (DeepSeek/Ollama/   │             │  + etcd + MinIO         │
│   千问/智谱)        │             │  (Docker Compose)       │
└────────────────────┘             └─────────────────────────┘
          │
┌─────────▼──────────┐  ┌──────────────────────────────────┐
│    PostgreSQL       │  │            Redis                 │
│  (Chat Memory /    │  │   (Session Cache / Hot Data)     │
│   Document Meta)   │  └──────────────────────────────────┘
└────────────────────┘
```

---

## 四、核心模块详细设计

### 4.1 持久化记忆 Chat 服务

#### 4.1.1 设计思路

Spring AI 2.0 提供 `ChatMemory` 抽象，内置 `InMemoryChatMemory` 和 `MessageWindowChatMemory`。项目在此基础上实现 **JDBC 持久化**，将对话记忆写入 PostgreSQL，并通过 Redis 做热会话缓存。

```
用户请求
   │
   ▼
ChatController
   │
   ▼
ChatClient (ChatMemoryAdvisor 注入)
   │
   ├─→ 加载历史消息 → Redis 缓存 → PostgreSQL（chat_messages 表）
   │
   ├─→ 调用 LLM ChatModel
   │
   └─→ 持久化新消息 → PostgreSQL → 更新 Redis
```

#### 4.1.2 数据库表设计

```sql
-- 会话表
CREATE TABLE chat_sessions (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL,
    title       VARCHAR(255),
    model       VARCHAR(64),
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 消息表
CREATE TABLE chat_messages (
    id           BIGSERIAL PRIMARY KEY,
    session_id   VARCHAR(64)   NOT NULL REFERENCES chat_sessions(id),
    role         VARCHAR(16)   NOT NULL,   -- user / assistant / system / tool
    content      TEXT          NOT NULL,
    token_count  INT,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_session ON chat_messages(session_id, created_at DESC);
```

#### 4.1.3 核心代码结构

```java
// 自定义 JDBC ChatMemory 实现
@Component
public class JdbcChatMemory implements ChatMemory {

    @Autowired private ChatMessageRepository messageRepo;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        messages.forEach(msg -> messageRepo.save(toEntity(conversationId, msg)));
        evictCache(conversationId);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String cacheKey = "chat:mem:" + conversationId;
        // 先查 Redis，未命中再查 DB
        ...
    }

    @Override
    public void clear(String conversationId) {
        messageRepo.deleteBySessionId(conversationId);
        evictCache(conversationId);
    }
}

// ChatClient 装配（使用 Advisor 模式）
@Bean
public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
    return ChatClient.builder(chatModel)
        .defaultAdvisors(
            new MessageChatMemoryAdvisor(chatMemory),   // 注入记忆
            new SimpleLoggerAdvisor()                    // 日志
        )
        .defaultSystem("你是一个专业的 AI 助手，请用中文回答。")
        .build();
}

// Controller 调用
@PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody ChatRequest req) {
    return chatClient.prompt()
        .user(req.getMessage())
        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, req.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
        .stream()
        .content();
}
```

---

### 4.2 MCP & Tools 服务

#### 4.2.1 设计思路

项目同时支持两种工具扩展模式：

- **Spring AI @Tool**：轻量级，适合项目内置工具（天气查询、数据库查询、文件操作等）
- **MCP Server 接入**：标准化协议，可对接外部 MCP Server（文件系统、浏览器、代码执行等）

```
ChatClient
   │
   ├─→ @Tool 注册工具（本地 Bean）
   │       ├── WeatherTool
   │       ├── DatabaseQueryTool
   │       ├── CalculatorTool
   │       └── KnowledgeSearchTool
   │
   └─→ McpSyncClient（外部 MCP Server）
           ├── filesystem MCP Server
           ├── brave-search MCP Server
           └── 自定义业务 MCP Server
```

#### 4.2.2 内置 Tool 实现示例

```java
// 工具函数定义
@Component
public class BusinessTools {

    @Tool(description = "根据城市名查询当前天气信息")
    public WeatherResult getWeather(
            @ToolParam(description = "城市名，如：北京、上海") String city) {
        // 调用天气 API
        return weatherService.query(city);
    }

    @Tool(description = "在知识库中搜索相关文档片段")
    public List<String> searchKnowledge(
            @ToolParam(description = "搜索关键词") String query,
            @ToolParam(description = "知识库ID") String knowledgeBaseId) {
        return ragService.search(query, knowledgeBaseId, 5);
    }

    @Tool(description = "执行数据库查询，返回业务数据")
    public String queryDatabase(
            @ToolParam(description = "查询意图描述") String intent) {
        // NL2SQL 转换后执行
        return databaseService.executeByIntent(intent);
    }
}

// 注册到 ChatClient
@Bean
public ChatClient chatClientWithTools(ChatModel chatModel,
                                      BusinessTools tools,
                                      McpSyncClient mcpClient) {
    return ChatClient.builder(chatModel)
        .defaultTools(tools)                              // @Tool 注册
        .defaultToolCallbacks(                            // MCP Tools 注册
            new McpToolCallbackProvider(mcpClient))
        .build();
}
```

#### 4.2.3 MCP Server 集成配置

```yaml
# application.yml - MCP 客户端配置
spring:
  ai:
    mcp:
      client:
        enabled: true
        connections:
          filesystem:
            type: stdio
            command: npx
            args: [ "@modelcontextprotocol/server-filesystem", "/data/workspace" ]
          brave-search:
            type: stdio
            command: npx
            args: [ "@modelcontextprotocol/server-brave-search" ]
            env:
              BRAVE_API_KEY: ${BRAVE_API_KEY}
```

---

### 4.3 RAG 知识库功能

#### 4.3.1 完整 RAG 管道设计

```
文档上传
   │
   ▼
┌─────────────────────────────────────────┐
│          文档解析 (DocumentReader)        │
│  TikaDocumentReader → PDF/Word/HTML     │
│  TextReader         → TXT/MD            │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│          文档切片 (TextSplitter)          │
│  TokenTextSplitter（按 Token 数切分）     │
│  参数：chunkSize=512, overlap=50         │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│        向量化 (EmbeddingModel)           │
│  DeepSeek text-embedding-3-small          │
│  或 Ollama nomic-embed-text（离线）      │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│       向量存储 (MilvusVectorStore)       │
│  Collection 按知识库隔离                  │
│  字段: id, content, embedding, metadata  │
└─────────────────────────────────────────┘

检索阶段：
用户问题 → Embedding → Milvus ANN 检索 → TopK 相关片段
         → QuestionAnswerAdvisor 注入上下文 → LLM 生成答案
```

#### 4.3.2 核心代码实现

```java
// RAG 导入服务
@Service
public class KnowledgeImportService {

    @Autowired private VectorStore vectorStore;
    @Autowired private EmbeddingModel embeddingModel;
    @Autowired private KnowledgeDocumentRepository docRepo;

    public void importDocument(MultipartFile file, String knowledgeBaseId) {
        // 1. 解析文档
        DocumentReader reader = resolveReader(file);
        List<Document> rawDocs = reader.get();

        // 2. 文档切片
        TextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(512)
            .withMinChunkSizeChars(100)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(1000)
            .withKeepSeparator(true)
            .build();
        List<Document> chunks = splitter.apply(rawDocs);

        // 3. 注入元数据
        chunks.forEach(chunk -> {
            chunk.getMetadata().put("knowledgeBaseId", knowledgeBaseId);
            chunk.getMetadata().put("fileName", file.getOriginalFilename());
            chunk.getMetadata().put("importTime", LocalDateTime.now().toString());
        });

        // 4. 向量化并存储到 Milvus
        vectorStore.add(chunks);

        // 5. 记录导入元数据到 PG
        saveDocumentMeta(file, knowledgeBaseId, chunks.size());
    }

    private DocumentReader resolveReader(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".html")) {
            return new TikaDocumentReader(file.getResource());
        }
        return new TextReader(file.getResource());
    }
}

// RAG 检索服务（QuestionAnswerAdvisor 方式）
@Service
public class RagChatService {

    @Autowired private ChatClient chatClient;
    @Autowired private VectorStore vectorStore;

    public Flux<String> ragChat(String question, String knowledgeBaseId, String sessionId) {

        // 构建过滤条件（按知识库隔离）
        FilterExpressionBuilder fb = new FilterExpressionBuilder();
        Filter.Expression filter = fb.eq("knowledgeBaseId", knowledgeBaseId).build();

        SearchRequest searchRequest = SearchRequest.builder()
            .topK(5)
            .similarityThreshold(0.7)
            .filterExpression(filter)
            .build();

        return chatClient.prompt()
            .user(question)
            .advisors(
                new QuestionAnswerAdvisor(vectorStore, searchRequest),  // RAG
                new MessageChatMemoryAdvisor(chatMemory,                // 记忆
                        sessionId, 10)
            )
            .stream()
            .content();
    }
}
```

#### 4.3.3 Milvus Collection Schema 设计

```java
// Milvus 自动建表（Spring AI 自动处理，下面是等效的 Schema 说明）
Collection: ai_vectors_{knowledgeBaseId}
┌─────────────────┬────────────────┬────────────────────────────────┐
│ 字段名           │ 类型            │ 说明                            │
├─────────────────┼────────────────┼────────────────────────────────┤
│ id              │ VARCHAR(64)    │ 主键，UUID                       │
│ content         │ VARCHAR(65535) │ 文档片段原文                      │
│ embedding       │ FLOAT_VECTOR   │ 向量，dim=1536（DeepSeek）          │
│ metadata        │ JSON           │ 元数据（fileName, pageNum 等）   │
│ knowledgeBaseId │ VARCHAR(64)    │ 标量过滤字段，知识库隔离           │
│ fileName        │ VARCHAR(512)   │ 来源文件名（标量索引）             │
│ create_time     │ INT64          │ 时间戳（用于时间范围过滤）          │
└─────────────────┴────────────────┴────────────────────────────────┘

索引策略：
- embedding 字段：IVF_FLAT 或 HNSW 索引，metric_type=IP（内积）
- knowledgeBaseId：标量索引（加速过滤）
```

#### 4.3.4 知识库元数据表（PostgreSQL）

```sql
-- 知识库表
CREATE TABLE knowledge_bases (
    id          VARCHAR(64)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    embed_model VARCHAR(64)  DEFAULT 'text-embedding-3-small',
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- 文档表
CREATE TABLE knowledge_documents (
    id               VARCHAR(64)  PRIMARY KEY,
    knowledge_base_id VARCHAR(64) NOT NULL REFERENCES knowledge_bases(id),
    file_name        VARCHAR(512) NOT NULL,
    file_type        VARCHAR(16),
    file_size        BIGINT,
    chunk_count      INT          DEFAULT 0,
    status           VARCHAR(16)  DEFAULT 'PROCESSING',  -- PROCESSING/DONE/FAILED
    error_msg        TEXT,
    created_at       TIMESTAMP    DEFAULT NOW()
);
```

---

## 五、项目目录结构

```
spring-ai-platform/
├── pom.xml                              # 父 POM（多模块管理）
├── docker-compose.yml                   # 一键启动基础设施
├── docker-compose.app.yml               # 启动应用服务
├── .env                                 # 环境变量（API Key 等）
│
├── ai-common/                           # 公共模块
│   └── src/main/java/
│       ├── dto/                         # 请求/响应 DTO
│       ├── exception/                   # 全局异常
│       └── utils/                       # 工具类
│
├── ai-chat/                             # Chat 模块
│   └── src/main/java/
│       ├── controller/
│       │   └── ChatController.java
│       ├── service/
│       │   ├── ChatService.java
│       │   └── SessionService.java
│       ├── memory/
│       │   ├── JdbcChatMemory.java      # 自定义持久化记忆
│       │   └── RedisCachedChatMemory.java
│       └── config/
│           └── ChatClientConfig.java
│
├── ai-tools/                            # MCP & Tools 模块
│   └── src/main/java/
│       ├── controller/
│       │   └── ToolsController.java
│       ├── tools/
│       │   ├── WeatherTool.java
│       │   ├── DatabaseQueryTool.java
│       │   └── KnowledgeSearchTool.java
│       ├── mcp/
│       │   ├── McpClientConfig.java
│       │   └── McpToolsService.java
│       └── config/
│           └── ToolsConfig.java
│
├── ai-rag/                              # RAG 知识库模块
│   └── src/main/java/
│       ├── controller/
│       │   ├── KnowledgeBaseController.java
│       │   └── RagChatController.java
│       ├── service/
│       │   ├── KnowledgeImportService.java
│       │   ├── KnowledgeSearchService.java
│       │   └── RagChatService.java
│       ├── entity/
│       │   ├── KnowledgeBase.java
│       │   └── KnowledgeDocument.java
│       └── config/
│           └── MilvusConfig.java
│
└── ai-gateway/                          # 统一入口（可选）
    └── src/main/java/
        └── filter/
            ├── AuthFilter.java
            └── RateLimitFilter.java
```

---

## 六、Docker Compose 集成方案

### 6.1 基础设施 docker-compose.yml

```yaml
version: "3.9"

services:
  # ── PostgreSQL ──────────────────────────────────────────────
  postgres:
    image: postgres:16-alpine
    container_name: ai-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ai_platform
      POSTGRES_USER: aiuser
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-ai_pass_2024}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U aiuser -d ai_platform"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ── Redis ────────────────────────────────────────────────────
  redis:
    image: redis:7-alpine
    container_name: ai-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD:-redis_2024}
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  # ── etcd（Milvus 元数据存储）──────────────────────────────────
  etcd:
    image: quay.io/coreos/etcd:v3.5.14
    container_name: milvus-etcd
    restart: unless-stopped
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
      - ETCD_SNAPSHOT_COUNT=50000
    volumes:
      - etcd_data:/etcd
    command: >
      etcd
      --advertise-client-urls=http://127.0.0.1:2379
      --listen-client-urls=http://0.0.0.0:2379
      --data-dir=/etcd

  # ── MinIO（Milvus 对象存储）──────────────────────────────────
  minio:
    image: minio/minio:RELEASE.2024-05-10T01-41-38Z
    container_name: milvus-minio
    restart: unless-stopped
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY:-minioadmin_2024}
    ports:
      - "9001:9001"
    volumes:
      - minio_data:/minio_data
    command: minio server /minio_data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

  # ── Milvus Standalone ────────────────────────────────────────
  milvus:
    image: milvusdb/milvus:v2.4.15
    container_name: milvus-standalone
    restart: unless-stopped
    command: ["milvus", "run", "standalone"]
    security_opt:
      - seccomp:unconfined
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    ports:
      - "19530:19530"   # gRPC
      - "9091:9091"     # HTTP / Metrics
    volumes:
      - milvus_data:/var/lib/milvus
    depends_on:
      etcd:
        condition: service_started
      minio:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9091/healthz"]
      interval: 30s
      timeout: 20s
      retries: 3

volumes:
  postgres_data:
  redis_data:
  etcd_data:
  minio_data:
  milvus_data:

networks:
  default:
    name: ai-network
    driver: bridge
```

### 6.2 应用服务 docker-compose.app.yml

```yaml
version: "3.9"

services:
  ai-platform:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ai-platform-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      # 数据库
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ai_platform
      SPRING_DATASOURCE_USERNAME: aiuser
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-ai_pass_2024}
      # Redis
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD:-redis_2024}
      # Milvus
      SPRING_AI_VECTORSTORE_MILVUS_HOST: milvus
      SPRING_AI_VECTORSTORE_MILVUS_PORT: 19530
      # LLM
      SPRING_AI_DeepSeek_API_KEY: ${DeepSeek_API_KEY}
      SPRING_AI_DeepSeek_BASE_URL: ${DeepSeek_BASE_URL:-https://xxx.com}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      milvus:
        condition: service_healthy
    networks:
      - ai-network

networks:
  ai-network:
    external: true
    name: ai-network
```

### 6.3 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# 复制打包产物
COPY target/ai-platform-*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"'

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

---

## 七、核心依赖配置 (pom.xml)

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.5</spring-boot.version>
    <spring-ai.version>2.0.0</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Spring AI Core -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-DeepSeek-spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI - Milvus 向量数据库 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-milvus-store-spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI - MCP 客户端 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
    </dependency>

    <!-- Spring AI - 文档解析 (Tika) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-tika-document-reader</artifactId>
    </dependency>

    <!-- Spring AI - Chat Memory JDBC -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-jdbc-chat-memory</artifactId>
    </dependency>

    <!-- 数据库驱动 -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 工具库 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 八、核心 application.yml 配置

```yaml
spring:
  application:
    name: ai-platform

  # 数据源
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_platform
    username: aiuser
    password: ai_pass_2024
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  # JPA
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

  # Redis
  data:
    redis:
      host: localhost
      port: 6379
      password: redis_2024

  # Spring AI 配置
  ai:
    # DeepSeek (或兼容接口)
    DeepSeek:
      api-key: ${DeepSeek_API_KEY}
      base-url: ${DeepSeek_BASE_URL:https://api.DeepSeek.com}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          max-tokens: 2048
      embedding:
        options:
          model: text-embedding-3-small

    # Milvus 向量数据库
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
        database-name: default
        collection-name: ai_vectors
        embedding-dimension: 1536        # DeepSeek embedding 维度
        index-type: IVF_FLAT
        metric-type: COSINE
        initialize-schema: true          # 自动建表

    # MCP 客户端
    mcp:
      client:
        enabled: true
        request-timeout: 30s

    # Chat Memory（使用 JDBC 实现）
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

---

## 九、API 接口设计

### 9.1 Chat 服务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/chat/sessions` | 创建新会话 |
| GET | `/api/v1/chat/sessions` | 获取会话列表 |
| DELETE | `/api/v1/chat/sessions/{id}` | 删除会话 |
| POST | `/api/v1/chat/completions` | 发送消息（SSE 流式） |
| GET | `/api/v1/chat/sessions/{id}/messages` | 获取历史消息 |

### 9.2 知识库接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/knowledge/bases` | 创建知识库 |
| GET | `/api/v1/knowledge/bases` | 知识库列表 |
| POST | `/api/v1/knowledge/bases/{id}/documents` | 上传文档（支持 PDF/Word/TXT） |
| GET | `/api/v1/knowledge/bases/{id}/documents` | 文档列表 |
| DELETE | `/api/v1/knowledge/documents/{id}` | 删除文档 |
| POST | `/api/v1/knowledge/search` | 向量检索（调试用） |
| POST | `/api/v1/rag/chat` | RAG 对话（SSE 流式） |

### 9.3 Tools 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/tools` | 已注册工具列表 |
| GET | `/api/v1/mcp/servers` | MCP Server 状态 |

---

## 十、开发里程碑规划

### Phase 1：基础环境搭建（第 1-2 周）

- [ ] 初始化 Maven 多模块项目结构
- [ ] 编写 docker-compose.yml，拉起 PG / Redis / Milvus
- [ ] 配置 Spring AI 2.0，验证 LLM 基础调用
- [ ] 实现基础 Chat 接口（无记忆版本）
- [ ] 完成本地开发环境文档

### Phase 2：Chat 持久化记忆（第 3-4 周）

- [ ] 实现 `JdbcChatMemory`，消息持久化到 PG
- [ ] Redis 缓存热会话数据
- [ ] 会话 CRUD 接口（创建、列表、删除）
- [ ] SSE 流式响应接口
- [ ] 多会话隔离测试

### Phase 3：MCP & Tools（第 5-6 周）

- [ ] 实现内置工具（Weather / DB Query / Calculator）
- [ ] 集成 MCP Client，接入 filesystem MCP Server
- [ ] Tool Calling 完整链路测试
- [ ] 工具注册管理接口

### Phase 4：RAG 知识库（第 7-9 周）

- [ ] 文档上传与解析（PDF / Word / TXT）
- [ ] TokenTextSplitter 切片，参数调优
- [ ] Embedding 生成并写入 Milvus
- [ ] 按知识库 ID 隔离向量检索
- [ ] `QuestionAnswerAdvisor` 接入，完成 RAG 对话
- [ ] 知识库 CRUD 接口

### Phase 5：联调优化与部署（第 10-12 周）

- [ ] 全链路集成测试（Chat + RAG + Tools 联合场景）
- [ ] 性能测试，Milvus 索引调优
- [ ] Dockerfile 及 docker-compose.app.yml 完善
- [ ] API 文档（Swagger/OpenAPI）
- [ ] 监控接入（Actuator + Prometheus）
- [ ] 灰度上线与验收

---

## 十一、风险评估与应对

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|---------|
| Spring AI 2.0 API 变化 | 中 | 高 | 锁定依赖版本，订阅官方 Release Notes |
| Milvus 集群稳定性 | 低 | 高 | 单节点 Standalone 部署，定期备份 Collection |
| Embedding 费用超支 | 中 | 中 | 引入本地 Ollama 离线 Embedding 作为备选 |
| LLM 延迟过高 | 中 | 中 | SSE 流式输出，前端逐字显示，体感改善 |
| 文档切片质量差 | 中 | 中 | A/B 测试不同 chunkSize，引入重排序（Rerank）|
| 向量检索召回率低 | 低 | 中 | 混合检索（向量 + 关键词），调整相似度阈值 |

---

## 十二、附录

### 12.1 本地快速启动

```bash
# 1. 克隆项目
git clone https://github.com/your-org/spring-ai-platform.git
cd spring-ai-platform

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填写 DeepSeek_API_KEY 等

# 3. 启动基础设施
docker compose up -d

# 4. 等待 Milvus 健康
docker compose ps

# 5. 启动应用
mvn spring-boot:run -pl ai-gateway
```

### 12.2 环境变量说明 (.env)

```bash
# LLM 配置
DeepSeek_API_KEY=sk-xxxxxxxxxxxx
DeepSeek_BASE_URL=https://api.DeepSeek.com    # 可替换为代理地址

# 数据库密码
POSTGRES_PASSWORD=ai_pass_2024
REDIS_PASSWORD=redis_2024
MINIO_SECRET_KEY=minioadmin_2024

# 可选：使用国内模型（DeepSeek 兼容接口）
# DeepSeek_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
# DeepSeek_API_KEY=sk-xxxx（通义千问 Key）
```

### 12.3 参考资源

- Spring AI 2.0 官方文档：https://docs.spring.io/spring-ai/reference/2.0/
- Milvus 官方文档：https://milvus.io/docs
- MCP 协议规范：https://modelcontextprotocol.io/
- Spring AI GitHub：https://github.com/spring-projects/spring-ai

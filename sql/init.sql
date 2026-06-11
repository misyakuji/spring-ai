CREATE TABLE IF NOT EXISTS chat_sessions (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL,
    title       VARCHAR(255),
    model       VARCHAR(64),
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id           BIGSERIAL PRIMARY KEY,
    session_id   VARCHAR(64)   NOT NULL REFERENCES chat_sessions(id),
    role         VARCHAR(16)   NOT NULL,
    content      TEXT          NOT NULL,
    token_count  INT,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages(session_id, created_at DESC);

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id          VARCHAR(64)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    embed_model VARCHAR(64)  DEFAULT 'text-embedding-3-small',
    created_at  TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS knowledge_documents (
    id                VARCHAR(64)  PRIMARY KEY,
    knowledge_base_id VARCHAR(64) NOT NULL REFERENCES knowledge_bases(id),
    file_name         VARCHAR(512) NOT NULL,
    file_type         VARCHAR(16),
    file_size         BIGINT,
    chunk_count       INT          DEFAULT 0,
    status            VARCHAR(16)  DEFAULT 'PROCESSING',
    error_msg         TEXT,
    created_at        TIMESTAMP    DEFAULT NOW()
);

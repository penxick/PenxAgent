
-- PenxAgent - Migration V3
-- Projects, chats and messages

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL
        REFERENCES users(id) ON DELETE CASCADE,

    name VARCHAR(150) NOT NULL,
    description TEXT,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT projects_name_not_blank
        CHECK (length(btrim(name)) > 0),

    CONSTRAINT projects_owner_id_id_unique
        UNIQUE (owner_id, id)
);

CREATE INDEX idx_projects_owner_created
    ON projects(owner_id, created_at DESC);


CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL
        REFERENCES users(id) ON DELETE CASCADE,

    project_id UUID,

    title VARCHAR(200) NOT NULL DEFAULT 'Новый чат',

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chats_title_not_blank
        CHECK (length(btrim(title)) > 0),

    CONSTRAINT chats_owner_id_id_unique
        UNIQUE (owner_id, id),

    CONSTRAINT chats_project_owner_fk
        FOREIGN KEY (owner_id, project_id)
        REFERENCES projects(owner_id, id)
);

CREATE INDEX idx_chats_owner_updated
    ON chats(owner_id, updated_at DESC);

CREATE INDEX idx_chats_project
    ON chats(project_id);


CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    chat_id UUID NOT NULL
        REFERENCES chats(id) ON DELETE CASCADE,

    role VARCHAR(20) NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT messages_role_check
        CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),

    CONSTRAINT messages_content_not_blank
        CHECK (length(btrim(content)) > 0)
);

CREATE INDEX idx_messages_chat_created
    ON messages(chat_id, created_at, id);

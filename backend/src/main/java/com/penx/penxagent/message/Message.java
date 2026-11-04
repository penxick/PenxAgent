
package com.penx.penxagent.message;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chat_id", nullable = false)
    private UUID chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }

    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public MessageRole getRole() { return role; }
    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.penx.penxagent.chat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatResponse(
    UUID id,
    UUID projectId,
    String title,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ChatResponse from(Chat chat) {
        return new ChatResponse(
            chat.getId(),
            chat.getProjectId(),
            chat.getTitle(),
            chat.getCreatedAt(),
            chat.getUpdatedAt()
        );
    }
}

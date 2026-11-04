
package com.penx.penxagent.message;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID chatId,
        String role,
        String content,
        OffsetDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}

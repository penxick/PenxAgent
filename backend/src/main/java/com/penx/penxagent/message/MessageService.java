
package com.penx.penxagent.message;

import com.penx.penxagent.chat.Chat;
import com.penx.penxagent.chat.ChatRepository;
import com.penx.penxagent.user.User;
import com.penx.penxagent.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messages;
    private final ChatRepository chats;
    private final UserRepository users;
    private final EntityManager entityManager;

    public MessageService(
            MessageRepository messages,
            ChatRepository chats,
            UserRepository users,
            EntityManager entityManager
    ) {
        this.messages = messages;
        this.chats = chats;
        this.users = users;
        this.entityManager = entityManager;
    }

    private UUID currentUserId(String principal) {
        UUID userId;

        try {
            userId = UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication"
            );
        }

        User user = users.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found or disabled"
                ));

        return user.getId();
    }

    private Chat ownedChat(UUID chatId, UUID ownerId) {
        return chats.findByIdAndOwnerId(chatId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Chat not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> list(
            String principal,
            UUID chatId
    ) {
        UUID ownerId = currentUserId(principal);
        ownedChat(chatId, ownerId);

        return messages.findByChatIdOrderByCreatedAtAscIdAsc(chatId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse create(
            String principal,
            UUID chatId,
            MessageCreateRequest request
    ) {
        UUID ownerId = currentUserId(principal);
        Chat chat = ownedChat(chatId, ownerId);

        Message message = new Message();
        message.setChatId(chatId);
        message.setRole(MessageRole.USER);
        message.setContent(request.content());

        messages.saveAndFlush(message);
        entityManager.refresh(message);

        chat.setUpdatedAt(OffsetDateTime.now());
        chats.saveAndFlush(chat);

        return MessageResponse.from(message);
    }

    
    @Transactional
    public MessageResponse saveAssistantMessage(
            UUID chatId,
            String content
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Assistant message cannot be empty"
            );
        }

        Chat chat = chats.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Chat not found"
                ));

        Message message = new Message();
        message.setChatId(chatId);
        message.setRole(MessageRole.ASSISTANT);
        message.setContent(content);

        messages.saveAndFlush(message);
        entityManager.refresh(message);

        chat.setUpdatedAt(OffsetDateTime.now());
        chats.saveAndFlush(chat);

        return MessageResponse.from(message);
    }

}

package com.penx.penxagent.chat;

import com.penx.penxagent.project.ProjectRepository;
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
public class ChatService {

    private final ChatRepository chats;
    private final ProjectRepository projects;
    private final UserRepository users;
    private final EntityManager entityManager;

    public ChatService(
        ChatRepository chats,
        ProjectRepository projects,
        UserRepository users,
        EntityManager entityManager
    ) {
        this.chats = chats;
        this.projects = projects;
        this.users = users;
        this.entityManager = entityManager;
    }

    private UUID currentUserId(String principal) {
        try {
            UUID id = UUID.fromString(principal);

            if (users.findById(id).isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User not found"
                );
            }

            return id;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid user session"
            );
        }
    }

    private Chat ownedChat(UUID chatId, UUID ownerId) {
        return chats.findByIdAndOwnerId(chatId, ownerId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Chat not found"
            ));
    }

    private void validateProject(UUID projectId, UUID ownerId) {
        if (projectId != null &&
            projects.findByIdAndOwnerId(projectId, ownerId).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Project not found"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> list(String principal, UUID projectId) {
        UUID ownerId = currentUserId(principal);

        if (projectId != null) {
            validateProject(projectId, ownerId);
        }

        List<Chat> result = projectId == null
            ? chats.findByOwnerIdOrderByUpdatedAtDesc(ownerId)
            : chats.findByOwnerIdAndProjectIdOrderByUpdatedAtDesc(
                ownerId, projectId
            );

        return result.stream().map(ChatResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ChatResponse get(String principal, UUID chatId) {
        UUID ownerId = currentUserId(principal);
        return ChatResponse.from(ownedChat(chatId, ownerId));
    }

    @Transactional
    public ChatResponse create(
        String principal, ChatCreateRequest request
    ) {
        UUID ownerId = currentUserId(principal);
        validateProject(request.projectId(), ownerId);

        Chat chat = new Chat();
        chat.setOwnerId(ownerId);
        chat.setProjectId(request.projectId());

        String title = request.title();
        chat.setTitle(
            title == null || title.isBlank()
                ? "овый чат"
                : title.trim()
        );

        chats.saveAndFlush(chat);
        entityManager.refresh(chat);

        return ChatResponse.from(chat);
    }

    @Transactional
    public ChatResponse rename(
        String principal, UUID chatId, ChatRenameRequest request
    ) {
        UUID ownerId = currentUserId(principal);
        Chat chat = ownedChat(chatId, ownerId);

        chat.setTitle(request.title().trim());
        chat.setUpdatedAt(OffsetDateTime.now());

        chats.saveAndFlush(chat);
        return ChatResponse.from(chat);
    }

    @Transactional
    public ChatResponse move(
        String principal, UUID chatId, ChatMoveRequest request
    ) {
        UUID ownerId = currentUserId(principal);
        validateProject(request.projectId(), ownerId);

        Chat chat = ownedChat(chatId, ownerId);
        chat.setProjectId(request.projectId());
        chat.setUpdatedAt(OffsetDateTime.now());

        chats.saveAndFlush(chat);
        return ChatResponse.from(chat);
    }

    @Transactional
    public void delete(String principal, UUID chatId) {
        UUID ownerId = currentUserId(principal);
        Chat chat = ownedChat(chatId, ownerId);
        chats.delete(chat);
    }
}


package com.penx.penxagent.project;

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
public class ProjectService {

    private final ProjectRepository projects;
    private final ChatRepository chats;
    private final UserRepository users;
    private final EntityManager entityManager;

    public ProjectService(
            ProjectRepository projects,
            ChatRepository chats,
            UserRepository users,
            EntityManager entityManager
    ) {
        this.projects = projects;
        this.chats = chats;
        this.users = users;
        this.entityManager = entityManager;
    }

    private UUID currentUserId(String authenticatedUserId) {
        UUID userId;

        try {
            userId = UUID.fromString(authenticatedUserId);
        } catch (IllegalArgumentException exception) {
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

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(String authenticatedUserId) {
        UUID ownerId = currentUserId(authenticatedUserId);

        return projects.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(
            String authenticatedUserId,
            UUID projectId
    ) {
        UUID ownerId = currentUserId(authenticatedUserId);

        Project project = projects.findByIdAndOwnerId(
                projectId, ownerId
        ).orElseThrow(this::notFound);

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse create(
            String authenticatedUserId,
            ProjectRequest request
    ) {
        UUID ownerId = currentUserId(authenticatedUserId);

        Project project = new Project();
        project.setOwnerId(ownerId);
        project.setName(request.name().trim());
        project.setDescription(request.description());

        projects.saveAndFlush(project);

        // Получаем значения дат, созданные PostgreSQL
        entityManager.refresh(project);

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(
            String authenticatedUserId,
            UUID projectId,
            ProjectRequest request
    ) {
        UUID ownerId = currentUserId(authenticatedUserId);

        Project project = projects.findByIdAndOwnerId(
                projectId, ownerId
        ).orElseThrow(this::notFound);

        project.setName(request.name().trim());
        project.setDescription(request.description());

        projects.saveAndFlush(project);

        // Обновляем дату изменения непосредственно в PostgreSQL
        entityManager.createNativeQuery(
                "UPDATE projects SET updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = :id AND owner_id = :ownerId"
        )
        .setParameter("id", projectId)
        .setParameter("ownerId", ownerId)
        .executeUpdate();

        entityManager.refresh(project);

        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(
            String authenticatedUserId,
            UUID projectId
    ) {
        UUID ownerId = currentUserId(authenticatedUserId);

        Project project = projects.findByIdAndOwnerId(
                projectId, ownerId
        ).orElseThrow(this::notFound);

        // Переносим чаты проекта в общий список
        chats.detachChatsFromProject(ownerId, projectId);

        // Удаляем только проект
        projects.delete(project);
        projects.flush();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project not found"
        );
    }
}

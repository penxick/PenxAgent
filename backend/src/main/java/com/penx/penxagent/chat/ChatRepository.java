package com.penx.penxagent.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    List<Chat> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    List<Chat> findByOwnerIdAndProjectIdOrderByUpdatedAtDesc(
        UUID ownerId, UUID projectId
    );

    Optional<Chat> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Chat c
        SET c.projectId = null,
            c.updatedAt = CURRENT_TIMESTAMP
        WHERE c.ownerId = :ownerId
          AND c.projectId = :projectId
        """)
    int detachChatsFromProject(
        @Param("ownerId") UUID ownerId,
        @Param("projectId") UUID projectId
    );
}

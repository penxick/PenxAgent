
package com.penx.penxagent.invitation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository
        extends JpaRepository<Invitation, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM Invitation i
            WHERE i.codeHash = :codeHash
            """)
    Optional<Invitation> findByCodeHashForUpdate(
            @Param("codeHash") String codeHash
    );
}

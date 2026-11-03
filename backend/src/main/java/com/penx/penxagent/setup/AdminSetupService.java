
package com.penx.penxagent.setup;

import com.penx.penxagent.user.Role;
import com.penx.penxagent.user.User;
import com.penx.penxagent.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AdminSetupService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public AdminSetupService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional
    public UUID createAdmin(CreateAdminRequest request) {

        // PostgreSQL transaction-level advisory lock.
        // Prevents simultaneous bootstrap requests.
        entityManager.createNativeQuery(
                "SELECT 1 FROM " +
                "(SELECT pg_advisory_xact_lock(20261103, 1)) AS setup_lock"
        ).getSingleResult();

        if (users.count() > 0) {
            throw new IllegalStateException(
                    "Initial setup has already been completed"
            );
        }

        String username = request.username().trim();
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);
        String displayName = request.displayName().trim();

        if (users.existsByUsername(username)
                || users.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Username or email already exists"
            );
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setDisplayName(displayName);
        admin.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        User saved = users.saveAndFlush(admin);

        return saved.getId();
    }
}

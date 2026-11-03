
package com.penx.penxagent.invitation;

import com.penx.penxagent.user.Role;
import com.penx.penxagent.user.User;
import com.penx.penxagent.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitations;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public InvitationService(
            InvitationRepository invitations,
            UserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.invitations = invitations;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public InvitationResponse createInvitation(
            UUID adminId,
            int expiresInHours
    ) {
        User admin = users.findById(adminId)
                .filter(User::isEnabled)
                .filter(user -> user.getRole() == Role.ADMIN)
                .orElseThrow(() ->
                        new SecurityException("Access denied"));

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String code = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        Invitation invitation = new Invitation();
        invitation.setCodeHash(hashCode(code));
        invitation.setCreatedBy(admin.getId());
        invitation.setExpiresAt(
                OffsetDateTime.now(ZoneOffset.UTC)
                        .plusHours(expiresInHours)
        );

        Invitation saved = invitations.saveAndFlush(invitation);

        return new InvitationResponse(
                saved.getId(),
                code,
                saved.getExpiresAt()
        );
    }

    @Transactional
    public UUID register(RegisterRequest request) {

        String code = request.invitationCode().trim();

        if (code.length() != 43
                || !code.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException(
                    "Invalid or expired invitation"
            );
        }

        Invitation invitation = invitations
                .findByCodeHashForUpdate(hashCode(code))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or expired invitation"));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (invitation.getUsedAt() != null
                || !invitation.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Invalid or expired invitation"
            );
        }

        String username = request.username().trim();
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);
        String displayName = request.displayName().trim();

        if (displayName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Display name is required"
            );
        }

        if (users.existsByUsername(username)
                || users.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Username or email already exists"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.USER);
        user.setEnabled(true);

        User saved = users.saveAndFlush(user);

        invitation.setUsedAt(now);
        invitation.setUsedBy(saved.getId());
        invitations.saveAndFlush(invitation);

        return saved.getId();
    }

    private String hashCode(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    code.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", exception
            );
        }
    }
}

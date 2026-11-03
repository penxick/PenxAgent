
package com.penx.penxagent.invitation;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(
            InvitationService invitationService
    ) {
        this.invitationService = invitationService;
    }

    @PostMapping("/admin/invitations")
    public ResponseEntity<?> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication
    ) {
        try {
            UUID adminId = UUID.fromString(
                    authentication.getName()
            );

            InvitationResponse response =
                    invitationService.createInvitation(
                            adminId,
                            request.expiresInHours()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (SecurityException exception) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            UUID userId = invitationService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "REGISTERED",
                            "userId", userId.toString(),
                            "role", "USER"
                    ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", exception.getMessage()));

        } catch (IllegalStateException
                 | DataIntegrityViolationException exception) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Username or email already exists"
                    ));
        }
    }
}

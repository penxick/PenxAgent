
package com.penx.penxagent.setup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/setup")
public class AdminSetupController {

    private final AdminSetupService setupService;
    private final boolean setupEnabled;
    private final String setupSecret;

    public AdminSetupController(
            AdminSetupService setupService,
            @Value("${penxagent.setup.enabled:false}")
            boolean setupEnabled,
            @Value("${penxagent.setup.secret:}")
            String setupSecret
    ) {
        this.setupService = setupService;
        this.setupEnabled = setupEnabled;
        this.setupSecret = setupSecret;
    }

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> createAdmin(
            @RequestHeader(
                    value = "X-PenxAgent-Setup-Secret",
                    required = false
            )
            String providedSecret,
            @Valid @RequestBody CreateAdminRequest body,
            HttpServletRequest request
    ) {

        if (!setupEnabled) {
            return error(
                    HttpStatus.NOT_FOUND,
                    "Setup is disabled"
            );
        }

        if (!isLocalRequest(request)) {
            return error(
                    HttpStatus.FORBIDDEN,
                    "Local access only"
            );
        }

        if (setupSecret.length() < 32
                || providedSecret == null
                || !MessageDigest.isEqual(
                        setupSecret.getBytes(StandardCharsets.UTF_8),
                        providedSecret.getBytes(StandardCharsets.UTF_8)
                )) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid setup secret"
            );
        }

        try {
            UUID userId = setupService.createAdmin(body);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "CREATED",
                            "userId", userId.toString(),
                            "role", "ADMIN"
                    ));

        } catch (IllegalStateException exception) {
            return error(
                    HttpStatus.CONFLICT,
                    exception.getMessage()
            );
        }
    }

    private boolean isLocalRequest(
            HttpServletRequest request
    ) {
        try {
            return InetAddress
                    .getByName(request.getRemoteAddr())
                    .isLoopbackAddress();
        } catch (Exception exception) {
            return false;
        }
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }
}

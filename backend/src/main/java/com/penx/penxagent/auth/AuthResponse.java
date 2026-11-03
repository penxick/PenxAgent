
package com.penx.penxagent.auth;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String role
) {
}

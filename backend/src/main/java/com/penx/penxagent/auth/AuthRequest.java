
package com.penx.penxagent.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String login,
        @NotBlank String password
) {
}

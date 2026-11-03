
package com.penx.penxagent.invitation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        String code,
        OffsetDateTime expiresAt
) {
}

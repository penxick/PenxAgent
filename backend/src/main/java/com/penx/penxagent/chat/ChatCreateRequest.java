package com.penx.penxagent.chat;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatCreateRequest(
    @Size(max = 200) String title,
    UUID projectId
) {}

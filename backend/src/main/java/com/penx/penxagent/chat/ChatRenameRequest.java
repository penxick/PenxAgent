package com.penx.penxagent.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRenameRequest(
    @NotBlank @Size(max = 200) String title
) {}

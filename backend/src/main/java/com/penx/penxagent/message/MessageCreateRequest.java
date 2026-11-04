
package com.penx.penxagent.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(
        @NotBlank
        @Size(max = 100000)
        String content
) {}

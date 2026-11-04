
package com.penx.penxagent.message;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public List<MessageResponse> list(
            Authentication authentication,
            @PathVariable UUID chatId
    ) {
        return service.list(
                authentication.getName(),
                chatId
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(
            Authentication authentication,
            @PathVariable UUID chatId,
            @Valid @RequestBody MessageCreateRequest request
    ) {
        return service.create(
                authentication.getName(),
                chatId,
                request
        );
    }
}

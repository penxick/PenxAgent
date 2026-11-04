package com.penx.penxagent.chat;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChatResponse> list(
        Authentication auth,
        @RequestParam(required = false) UUID projectId
    ) {
        return service.list(auth.getName(), projectId);
    }

    @GetMapping("/{id}")
    public ChatResponse get(
        Authentication auth,
        @PathVariable UUID id
    ) {
        return service.get(auth.getName(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponse create(
        Authentication auth,
        @Valid @RequestBody ChatCreateRequest request
    ) {
        return service.create(auth.getName(), request);
    }

    @PatchMapping("/{id}")
    public ChatResponse rename(
        Authentication auth,
        @PathVariable UUID id,
        @Valid @RequestBody ChatRenameRequest request
    ) {
        return service.rename(auth.getName(), id, request);
    }

    @PutMapping("/{id}/project")
    public ChatResponse move(
        Authentication auth,
        @PathVariable UUID id,
        @Valid @RequestBody ChatMoveRequest request
    ) {
        return service.move(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        Authentication auth,
        @PathVariable UUID id
    ) {
        service.delete(auth.getName(), id);
    }
}

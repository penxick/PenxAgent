
package com.penx.penxagent.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectResponse> list(
            Authentication authentication
    ) {
        return service.list(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return service.get(authentication.getName(), id);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        ProjectResponse project = service.create(
                authentication.getName(), request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(project);
    }

    @PatchMapping("/{id}")
    public ProjectResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        return service.update(
                authentication.getName(), id, request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        service.delete(authentication.getName(), id);

        return ResponseEntity.noContent().build();
    }
}

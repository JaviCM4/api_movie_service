package com.example.movies.controllers;

import com.example.movies.dtos.actor.request.CreateActorRequest;
import com.example.movies.dtos.actor.request.UpdateActorRequest;
import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.actor.ActorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/actors")
public class ActorController {

    private final ActorService actorService;

    @Autowired
    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<ActorResponse>> listActors() {
        return ResponseEntity.ok(actorService.findAllActor());
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ActorResponse> createActor(
            @Valid @RequestBody CreateActorRequest request) throws ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED).body(actorService.createActor(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ActorResponse> updateActor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateActorRequest request) throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.ok(actorService.updateActor(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ActorResponse> toggleActor(@PathVariable UUID id) throws ResourceNotFoundException {
        return ResponseEntity.ok(actorService.toggleActor(id));
    }
}

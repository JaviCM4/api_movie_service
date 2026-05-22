package com.example.movies.controllers;

import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.services.actor.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/actors")
public class ActorController {

    private final ActorService actorService;

    @Autowired
    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ActorResponse>> listActors() {
        return ResponseEntity.ok(actorService.findAllActor());
    }
}

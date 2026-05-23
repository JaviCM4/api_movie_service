package com.example.movies.controllers;

import com.example.movies.dtos.people.request.CreatePeopleRequest;
import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.people.PeopleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/people")
public class PeopleController {

    private final PeopleService peopleService;

    @Autowired
    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<PeopleResponse>> getAll() {
        return ResponseEntity.ok(peopleService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<PeopleResponse> createPeople(
            @Valid @RequestBody CreatePeopleRequest request) throws ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED).body(peopleService.createPeople(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<PeopleResponse> updatePeople(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePeopleRequest request) throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.ok(peopleService.updatePeople(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<PeopleResponse> togglePeople(@PathVariable UUID id) throws ResourceNotFoundException {
        return ResponseEntity.ok(peopleService.togglePeople(id));
    }
}

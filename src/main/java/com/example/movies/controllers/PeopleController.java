package com.example.movies.controllers;

import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.people.PeopleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    public ResponseEntity<List<PeopleResponse>> getAll() {
        return ResponseEntity.ok(peopleService.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PeopleResponse> updatePeople(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePeopleRequest request)
            throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.ok(peopleService.updatePeople(id, request));
    }
}

package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateCastRequest;
import com.example.movies.dtos.movie.request.UpdateCastRequest;
import com.example.movies.dtos.movie.response.CastResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.CastService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies/{movieId}/cast")
public class CastController {

    private final CastService castService;

    @Autowired
    public CastController(CastService castService) {
        this.castService = castService;
    }

    @GetMapping
    public ResponseEntity<List<CastResponse>> getCast(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(castService.getCast(movieId));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<CastResponse>> addActor(@PathVariable UUID movieId,
                                                        @Valid @RequestBody CreateCastRequest request)
            throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED).body(castService.addActor(movieId, request));
    }

    @PatchMapping("/{castId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<CastResponse> updateCharacterName(@PathVariable UUID movieId,
                                                             @PathVariable UUID castId,
                                                             @Valid @RequestBody UpdateCastRequest request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(castService.updateCharacterName(castId, request));
    }

    @DeleteMapping("/{castId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<CastResponse>> removeActor(@PathVariable UUID movieId,
                                                           @PathVariable UUID castId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(castService.removeActor(movieId, castId));
    }
}

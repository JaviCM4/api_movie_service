package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreatePosterRequest;
import com.example.movies.dtos.movie.request.UpdatePosterRequest;
import com.example.movies.dtos.movie.response.PosterResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import com.example.movies.services.movie.inteface.PosterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies/{movieId}/posters")
public class PosterController {

    private final PosterService posterService;

    @Autowired
    public PosterController(PosterService posterService) {
        this.posterService = posterService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<PosterResponse>> getPosters(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(posterService.getPosters(movieId));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<PosterResponse>> addPoster(@PathVariable UUID movieId, @Valid @RequestBody CreatePosterRequest request)
            throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(posterService.addPoster(movieId, request));
    }

    @PatchMapping("/main")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<PosterResponse>> setMainPoster(@PathVariable UUID movieId, @Valid @RequestBody UpdatePosterRequest request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(posterService.setMainPoster(movieId, request));
    }

    @DeleteMapping("/{posterId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<PosterResponse>> deletePoster(@PathVariable UUID movieId, @PathVariable UUID posterId)
            throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.ok(posterService.deletePoster(posterId));
    }
}

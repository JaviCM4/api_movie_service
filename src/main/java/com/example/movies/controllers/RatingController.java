package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/movies/{movieId}/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<RatingSummaryResponse> getRatings(@PathVariable UUID movieId) throws ResourceNotFoundException {
        return ResponseEntity.ok(ratingService.findRatingsByMovie(movieId));
    }

    @PostMapping
    public ResponseEntity<RatingSummaryResponse> createRating(
            @PathVariable UUID movieId,
            @Valid @RequestBody CreateRatingRequest request) throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.createRating(movieId, request));
    }

    @PatchMapping("/{ratingId}")
    public ResponseEntity<RatingSummaryResponse> updateRating(
            @PathVariable UUID movieId,
            @PathVariable UUID ratingId,
            @Valid @RequestBody UpdateRatingRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(ratingService.updateRating(ratingId, request));
    }
}

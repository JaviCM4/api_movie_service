package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.dtos.movie.response.UserMovieRatingResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/v1/movies/{movieId}/ratings")
    public ResponseEntity<RatingSummaryResponse> getRatings(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ratingService.findRatingsByMovie(movieId));
    }

    @PostMapping("/v1/movies/{movieId}/ratings")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RatingSummaryResponse> createRating(
            @PathVariable UUID movieId,
            @Valid @RequestBody CreateRatingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws ConflictException, ResourceNotFoundException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.createRating(movieId, userId, request));
    }

    @PatchMapping("/v1/ratings/{ratingId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RatingSummaryResponse> updateRating(
            @PathVariable UUID ratingId,
            @Valid @RequestBody UpdateRatingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws ResourceNotFoundException, ConflictException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ratingService.updateRating(ratingId, userId, request));
    }

    @GetMapping("/v1/ratings/user")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<UserMovieRatingResponse>> getMyRatings(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ratingService.findRatingsByUser(userId));
    }
}

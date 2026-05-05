package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateMovieRequest;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.request.UpdateMovieRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieDetailResponse>> getMovies(@RequestParam UUID countryId) {
        return ResponseEntity.ok(movieService.findAllMoviesByCountry(countryId));
    }

    @PostMapping
    public ResponseEntity<Void> createMovie(@Valid @RequestBody CreateMovieRequest request)
            throws ResourceNotFoundException, ConflictException {
        movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{movieId}")
    public ResponseEntity<Void> updateMovie(
            @PathVariable UUID movieId,
            @Valid @RequestBody UpdateMovieRequest request)
            throws ResourceNotFoundException {
        movieService.updateMovie(movieId, request);
        return ResponseEntity.noContent().build();
    }
}

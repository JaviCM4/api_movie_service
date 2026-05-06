package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateMovieRequest;
import com.example.movies.dtos.movie.request.UpdateMovieRequest;
import com.example.movies.dtos.movie.response.MovieAdminResponse;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.response.MovieSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieSummaryResponse>> getMovies(
            @RequestParam UUID countryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID classificationId,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(movieService.findAllMoviesByCountry(countryId, title, categoryId, classificationId, sort));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDetailResponse> getMovieDetail(@PathVariable UUID movieId, @RequestParam UUID countryId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(movieService.findMovieById(movieId, countryId));
    }

    @PostMapping
    public ResponseEntity<Void> createMovie(@Valid @RequestBody CreateMovieRequest request)
            throws ConflictException, ResourceNotFoundException {
        movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{movieId}/admin")
    public ResponseEntity<MovieAdminResponse> getMovieAdmin(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(movieService.findMovieAdminById(movieId));
    }

    @PatchMapping("/{movieId}")
    public ResponseEntity<Void> updateMovie(@PathVariable UUID movieId, @Valid @RequestBody UpdateMovieRequest request)
            throws ResourceNotFoundException {
        movieService.updateMovie(movieId, request);
        return ResponseEntity.noContent().build();
    }
}

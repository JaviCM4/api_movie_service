package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.AssignPeopleRequest;
import com.example.movies.dtos.movie.response.MoviePeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.services.movie.inteface.MoviePeopleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies/{movieId}/people")
public class MoviePeopleController {

    private final MoviePeopleService moviePeopleService;

    @Autowired
    public MoviePeopleController(MoviePeopleService moviePeopleService) {
        this.moviePeopleService = moviePeopleService;
    }

    @GetMapping
    public ResponseEntity<List<MoviePeopleResponse>> getPeople(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(moviePeopleService.getPeople(movieId));
    }

    @PostMapping
    public ResponseEntity<List<MoviePeopleResponse>> addPerson(@PathVariable UUID movieId,
                                                               @Valid @RequestBody AssignPeopleRequest request)
            throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED).body(moviePeopleService.addPerson(movieId, request));
    }

    @PatchMapping("/{moviePeopleId}/rol")
    public ResponseEntity<List<MoviePeopleResponse>> updateRol(@PathVariable UUID movieId,
                                                               @PathVariable UUID moviePeopleId,
                                                               @RequestParam RolMovieEnum rol)
            throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.ok(moviePeopleService.updateRol(movieId, moviePeopleId, rol));
    }

    @DeleteMapping("/{moviePeopleId}")
    public ResponseEntity<List<MoviePeopleResponse>> removePerson(@PathVariable UUID movieId,
                                                                   @PathVariable UUID moviePeopleId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(moviePeopleService.removePerson(movieId, moviePeopleId));
    }
}

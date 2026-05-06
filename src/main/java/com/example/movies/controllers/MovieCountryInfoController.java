package com.example.movies.controllers;

import com.example.movies.dtos.movie.response.MovieCountryInfoResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieCountryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies/{movieId}/country-info")
public class MovieCountryInfoController {

    private final MovieCountryInfoService movieCountryInfoService;

    @Autowired
    public MovieCountryInfoController(MovieCountryInfoService movieCountryInfoService) {
        this.movieCountryInfoService = movieCountryInfoService;
    }

    @GetMapping
    public ResponseEntity<List<MovieCountryInfoResponse>> getCountryInfo(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(movieCountryInfoService.getCountryInfo(movieId));
    }

    @PostMapping("/{classificationId}")
    public ResponseEntity<List<MovieCountryInfoResponse>> addClassification(@PathVariable UUID movieId,
                                                                             @PathVariable UUID classificationId)
            throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieCountryInfoService.addClassification(movieId, classificationId));
    }

    @PatchMapping("/{movieCountryInfoId}/toggle")
    public ResponseEntity<MovieCountryInfoResponse> toggleActive(@PathVariable UUID movieId,
                                                                  @PathVariable UUID movieCountryInfoId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(movieCountryInfoService.toggleActive(movieCountryInfoId));
    }

    @DeleteMapping("/{movieCountryInfoId}")
    public ResponseEntity<List<MovieCountryInfoResponse>> removeClassification(@PathVariable UUID movieId,
                                                                                @PathVariable UUID movieCountryInfoId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(movieCountryInfoService.removeClassification(movieId, movieCountryInfoId));
    }
}

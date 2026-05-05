package com.example.movies.controllers;

import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.dtos.movie.request.MovieCategoryRequest;
import com.example.movies.services.movie.MovieCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies/{movieId}/categories")
public class MovieCategoryController {

    private final MovieCategoryService movieCategoryService;

    public MovieCategoryController(MovieCategoryService movieCategoryService) {
        this.movieCategoryService = movieCategoryService;
    }

    @PostMapping
    public ResponseEntity<List<CategoryResponse>> addCategory(
            @PathVariable UUID movieId,
            @Valid @RequestBody MovieCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieCategoryService.addCategory(movieId, request.getCategoryId()));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<List<CategoryResponse>> removeCategory(
            @PathVariable UUID movieId,
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(movieCategoryService.removeCategory(movieId, categoryId));
    }
}

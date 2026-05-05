package com.example.movies.controllers;

import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.dtos.movie.request.MovieCategoryRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies/{movieId}/categories")
public class MovieCategoryController {

    private final MovieCategoryService movieCategoryService;

    @Autowired
    public MovieCategoryController(MovieCategoryService movieCategoryService) {
        this.movieCategoryService = movieCategoryService;
    }

    @PostMapping
    public ResponseEntity<List<CategoryResponse>> addCategory(@PathVariable UUID movieId, @Valid @RequestBody MovieCategoryRequest request)
            throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieCategoryService.addCategory(movieId, request.getCategoryId()));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<List<CategoryResponse>> removeCategory(@PathVariable UUID movieId, @PathVariable UUID categoryId)
            throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.ok(movieCategoryService.removeCategory(movieId, categoryId));
    }
}

package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface MovieCategoryService {

    List<CategoryResponse> getCategories(UUID movieId) throws ResourceNotFoundException;

    List<CategoryResponse> addCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException;

    List<CategoryResponse> removeCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException;
}

package com.example.movies.services.category;

import com.example.movies.dtos.category.request.CreateCategoryRequest;
import com.example.movies.dtos.category.request.UpdateCategoryRequest;
import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest dto) throws ConflictException;

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest dto) throws ResourceNotFoundException, ConflictException;

    CategoryResponse toggleActive(UUID id) throws ResourceNotFoundException;

    List<CategoryResponse> findAllActive();
}

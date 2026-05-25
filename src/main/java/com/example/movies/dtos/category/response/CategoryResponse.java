package com.example.movies.dtos.category.response;

import com.example.movies.models.category.Category;
import lombok.Value;

import java.util.UUID;

@Value
public class CategoryResponse {

    UUID id;
    String name;
    boolean isActive;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive()
        );
    }
}

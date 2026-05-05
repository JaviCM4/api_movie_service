package com.example.movies.dtos.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateCategoryRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    String name;
}

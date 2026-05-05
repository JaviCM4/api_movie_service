package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class MovieCategoryRequest {

    @NotNull
    UUID categoryId;
}

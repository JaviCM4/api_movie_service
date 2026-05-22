package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class MovieCategoryRequest {

    @NotNull(message = "El id de la categoría es obligatorio")
    UUID categoryId;
}

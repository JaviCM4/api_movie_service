package com.example.movies.dtos.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre de la categoría debe tener entre 1 y 100 caracteres")
    String name;
}

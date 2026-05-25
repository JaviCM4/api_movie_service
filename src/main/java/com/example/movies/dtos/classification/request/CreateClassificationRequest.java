package com.example.movies.dtos.classification.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateClassificationRequest {

    @NotBlank(message = "El nombre de la clasificación es obligatorio")
    @Size(min = 1, max = 20, message = "El nombre de la clasificación debe tener entre 1 y 20 caracteres")
    String name;

    @NotNull(message = "El límite de edad es obligatorio")
    @Min(value = 0, message = "El límite de edad no puede ser negativo")
    Integer ageLimit;
}

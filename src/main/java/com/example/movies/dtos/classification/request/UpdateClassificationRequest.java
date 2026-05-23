package com.example.movies.dtos.classification.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateClassificationRequest {

    @Size(min = 1, max = 20, message = "El nombre de la clasificación debe tener entre 1 y 20 caracteres")
    String name;

    @Min(value = 0, message = "El límite de edad no puede ser negativo")
    Integer ageLimit;
}

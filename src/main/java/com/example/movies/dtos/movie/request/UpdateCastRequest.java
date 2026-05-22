package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCastRequest {

    @NotBlank(message = "El nombre del personaje es obligatorio")
    @Size(max = 255, message = "El nombre del personaje no puede superar los 255 caracteres")
    String characterName;
}

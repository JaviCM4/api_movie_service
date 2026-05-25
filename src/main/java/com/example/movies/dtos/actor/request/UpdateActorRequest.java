package com.example.movies.dtos.actor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class UpdateActorRequest {

    @NotBlank(message = "El nombre del actor es obligatorio")
    @Size(max = 75, message = "El nombre del actor no puede superar los 75 caracteres")
    String name;

    @URL(message = "La imagen debe ser una URL válida")
    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
    String urlImage;
}

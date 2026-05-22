package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Value
public class UpdateMovieRequest {

    @Size(min = 2, max = 75, message = "El título debe tener entre 2 y 75 caracteres")
    String title;

    @Size(min = 2, max = 255, message = "La sinopsis debe tener entre 2 y 255 caracteres")
    String synopsis;

    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    Integer duration;

    @URL(message = "El enlace del tráiler debe ser una URL válida")
    @Size(max = 500, message = "El enlace del tráiler no puede superar los 500 caracteres")
    String trailerLink;

    @Size(min = 2, max = 50, message = "El idioma original debe tener entre 2 y 50 caracteres")
    String originalLanguage;

    @Future(message = "La fecha de estreno debe ser una fecha futura")
    LocalDate releaseDate;

    Boolean allowComments;

    Boolean allowRatings;
}

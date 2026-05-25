package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.Movie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
public class CreateMovieRequest {

    @NotNull(message = "Las clasificaciones son obligatorias")
    @Size(min = 1, message = "Debe incluir al menos una clasificación")
    List<UUID> classificationIds;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 75, message = "El título debe tener entre 2 y 75 caracteres")
    String title;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(min = 2, max = 255, message = "La sinopsis debe tener entre 2 y 255 caracteres")
    String synopsis;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    Integer duration;

    @URL(message = "El enlace del tráiler debe ser una URL válida")
    @Size(max = 500, message = "El enlace del tráiler no puede superar los 500 caracteres")
    String trailerLink;

    @NotBlank(message = "El idioma original es obligatorio")
    @Size(min = 2, max = 50, message = "El idioma original debe tener entre 2 y 50 caracteres")
    String originalLanguage;

    @NotNull(message = "La fecha de estreno es obligatoria")
    @Future(message = "La fecha de estreno debe ser una fecha futura")
    LocalDate releaseDate;

    @Valid
    List<AssignActorRequest> actors;

    List<UUID> categories;

    @Valid
    List<CreatePosterRequest> posters;

    @Valid
    List<AssignPeopleRequest> people;

    public Movie createEntity() {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setSynopsis(synopsis);
        movie.setDuration(duration);
        movie.setTrailerLink(trailerLink);
        movie.setOriginalLanguage(originalLanguage);
        movie.setReleaseDate(releaseDate);
        return movie;
    }
}

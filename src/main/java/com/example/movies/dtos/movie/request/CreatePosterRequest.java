package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.Poster;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class CreatePosterRequest {

    @NotNull(message = "La URL del póster es obligatoria")
    @URL(message = "La URL del póster debe ser válida")
    @Size(max = 500, message = "La URL del póster no puede superar los 500 caracteres")
    String urlImagen;

    @NotNull
    boolean isMain;

    public Poster createEntity(Movie movie) {
        Poster poster = new Poster();
        poster.setMovie(movie);
        poster.setUrlImage(urlImagen);
        poster.setMain(isMain);
        return poster;
    }
}

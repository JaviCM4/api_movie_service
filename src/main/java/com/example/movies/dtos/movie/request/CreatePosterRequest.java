package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.Poster;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class CreatePosterRequest {

    @NotNull
    @URL
    @Size(max = 500)
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

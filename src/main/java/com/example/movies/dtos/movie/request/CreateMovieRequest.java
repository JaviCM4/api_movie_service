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

    @NotNull
    @Size(min = 1)
    List<UUID> classificationIds;

    @NotBlank
    @Size(min = 2, max = 75)
    String title;

    @NotBlank
    @Size(min = 2, max = 255)
    String synopsis;

    @NotNull
    @Min(1)
    Integer duration;

    @URL
    @Size(max = 500)
    String trailerLink;

    @NotBlank
    @Size(min = 2, max = 50)
    String originalLanguage;

    @NotNull
    @Future
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

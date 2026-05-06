package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Value
public class UpdateMovieRequest {

    @Size(min = 2, max = 75)
    String title;

    @Size(min = 2, max = 255)
    String synopsis;

    @Min(1)
    Integer duration;

    @URL
    @Size(max = 500)
    String trailerLink;

    @Size(min = 2, max = 50)
    String originalLanguage;

    @Future
    LocalDate releaseDate;

    Boolean allowComments;

    Boolean allowRatings;
}

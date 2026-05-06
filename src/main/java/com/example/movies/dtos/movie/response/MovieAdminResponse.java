package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Movie;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class MovieAdminResponse {

    UUID id;
    String title;
    String synopsis;
    Integer duration;
    String trailerLink;
    String originalLanguage;
    LocalDate releaseDate;
    boolean allowComments;
    boolean allowRatings;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static MovieAdminResponse from(Movie movie) {
        return new MovieAdminResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getSynopsis(),
                movie.getDuration(),
                movie.getTrailerLink(),
                movie.getOriginalLanguage(),
                movie.getReleaseDate(),
                movie.isAllowComments(),
                movie.isAllowRatings(),
                movie.getCreatedAt(),
                movie.getUpdatedAt()
        );
    }
}

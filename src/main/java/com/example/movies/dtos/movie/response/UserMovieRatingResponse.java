package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieRating;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class UserMovieRatingResponse {

    UUID id;
    Short score;
    LocalDateTime createdAt;
    boolean edited;
    UUID movieId;
    String movieTitle;
    String posterUrl;

    public static UserMovieRatingResponse from(MovieRating rating, String posterUrl) {
        return new UserMovieRatingResponse(
                rating.getId(),
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt() != null,
                rating.getMovie().getId(),
                rating.getMovie().getTitle(),
                posterUrl
        );
    }
}

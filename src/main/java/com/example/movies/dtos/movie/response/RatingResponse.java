package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieRating;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class RatingResponse {

    UUID id;
    UUID userId;
    String userName;
    Short score;
    LocalDateTime createdAt;
    boolean edited;

    public static RatingResponse from(MovieRating rating, String userName) {
        return new RatingResponse(
                rating.getId(),
                rating.getUserId(),
                userName,
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt() != null
        );
    }
}

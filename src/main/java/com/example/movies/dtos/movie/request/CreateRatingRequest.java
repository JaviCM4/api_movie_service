package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.MovieRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    Short score;

    public MovieRating createEntity(UUID userId) {
        MovieRating rating = new MovieRating();
        rating.setUserId(userId);
        rating.setScore(score);
        return rating;
    }
}

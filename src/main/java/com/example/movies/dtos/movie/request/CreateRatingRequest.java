package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.MovieRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateRatingRequest {

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    Short score;

    public MovieRating createEntity(UUID userId) {
        MovieRating rating = new MovieRating();
        rating.setUserId(userId);
        rating.setScore(score);
        return rating;
    }
}

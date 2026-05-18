package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateRatingRequest {

    @NotNull
    UUID userId;

    @NotNull
    @Min(1)
    @Max(5)
    Short score;
}

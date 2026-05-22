package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    Short score;
}

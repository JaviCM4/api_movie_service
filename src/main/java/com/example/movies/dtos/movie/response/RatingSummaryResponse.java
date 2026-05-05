package com.example.movies.dtos.movie.response;

import lombok.Value;

import java.util.List;

@Value
public class RatingSummaryResponse {

    List<RatingResponse> ratings;
    Double averageScore;
}
